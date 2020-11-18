package com.wangjiang.devops.batchjob;

import com.wangjiang.devops.batchjob.constant.TaskConstant;
import com.wangjiang.devops.batchjob.constant.TaskResultType;
import com.wangjiang.devops.batchjob.exception.CreateTaskException;
import com.wangjiang.devops.batchjob.job.BaseJob;
import com.wangjiang.devops.batchjob.job.MonitorJob;
import com.wangjiang.devops.batchjob.job.ScheduledJob;
import com.wangjiang.devops.batchjob.task.*;
import com.wangjiang.devops.batchjob.vo.TaskResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;


/**
 * 批量任务池
 *
 * @author wangjiang
 * @date 2020-11-12 11:22:20
 */
public class BatchJobPool {
    private static final Logger log = LoggerFactory.getLogger(BatchJobPool.class);

    // 只用核心数的一半， 最少为2个
    private static final int THREAD_COUNTS =
            Runtime.getRuntime().availableProcessors() / 2 > 3 ? Runtime.getRuntime().availableProcessors() / 2 : 3;

    private Lock lock = new ReentrantLock();

    // 默认延时任务过期时间 12000s
    private long defaultDelayJobExpireMillSecond = 12000;
    // 任务执行线程池
    // 线程池，固定大小，有界队列
    private ExecutorService taskExecutor =
            new ThreadPoolExecutor(3, THREAD_COUNTS, 60,
                    TimeUnit.SECONDS, new ArrayBlockingQueue<>(100));

    // 任务列表 #wangjiang 2020-11-12 11:59:53#
    private ConcurrentHashMap<String, BaseJob<?, ?>> jobInfoMap
            = new ConcurrentHashMap<>();

    // 单任务执行结果处理 #wangjiang 2020-11-12 01:10:37#
    private TaskResultHandler taskResultHandler;

    // 监控任务
    private MonitorJob monitorJob;

    public BatchJobPool(ExecutorService taskExecutor) {
        if (Objects.isNull(taskExecutor)) {
            this.taskExecutor = new ThreadPoolExecutor(3, THREAD_COUNTS, 60,
                    TimeUnit.SECONDS, new ArrayBlockingQueue<>(100));
        } else {
            this.taskExecutor = taskExecutor;
        }
        initMonitorJob();
    }


    public BatchJobPool() {
        taskExecutor = new ThreadPoolExecutor(3, THREAD_COUNTS, 60,
                TimeUnit.SECONDS, new ArrayBlockingQueue<>(100));
        initMonitorJob();
    }

    /**
     * 初始化
     *
     * @author wangjiang
     * @date 2020-11-13 08:49:20
     */
    private void initMonitorJob() {
        log.info("batchjob inited...");
        monitorJob = new MonitorJob(this);
        // 执行任务
        Thread thread = new Thread(monitorJob);
        thread.setName("fetchJob_monitor_thread");
        thread.setDaemon(true);
        thread.start();
        log.info("开启批量任务过期检查守护线程................");
    }


    /**
     * 注册任务
     *
     * @author wangjiang
     * @date 2020-11-12 02:03:53
     */
    public void registerJob(BaseJob jobInfo) {
        if (jobInfoMap.containsKey(jobInfo.getName())) {
            log.error("current job[{}] exist in pool", jobInfo.getName());
            throw new CreateTaskException("current job [" + jobInfo.getName() + "] exist in pool");
        }

        log.info("register a job [{}]", jobInfo.getName());
        if (jobInfoMap.size() >= 2) {
            log.error("current job[{}] error too many jobs", jobInfo.getName());
                throw new CreateTaskException("can't create too many jobs");
        }

        jobInfo.setTaskExecutor(taskExecutor);
        jobInfo.setMonitor(monitorJob);
        // 这里存在并发问题
        final Lock lock = this.lock;
        try {
            lock.lock();
            jobInfoMap.put(jobInfo.getName(), jobInfo);
            CompletableFuture.runAsync(jobInfo, taskExecutor);
        } catch (Exception e) {
            log.error("执行错误", e);
        } finally {
            lock.unlock();
        }
    }


    public ExecutorService getTaskExecutor() {
        return taskExecutor;
    }


    /**
     * 往任务中放入单个执行任务， 放入即执行
     * 支持回调
     * failureActiveTime 失败后的执行时间 null 代表这个参数无效
     *
     * @author wangjiang
     * @date 2020-11-12 01:31:00
     */
    public <T, R> void putTask(String jobName, T t, Consumer<TaskResult<R>> callback) {
        BaseJob<T, R> jobInfo = getJob(jobName);
        // 制作任务
        SingleTask<T, R> task = new SingleTask(jobInfo, t, jobInfo.getFailureActiveTime(), callback);
        jobInfo.putTask(task);
    }

    /**
     * 放入单次执行的延时任务
     * 执行数据
     *
     * @param delayMillSeconds 延时多长时间执行 毫秒
     * @author wangjiang
     * @date 2020-11-16 11:51:25
     */
    public <T, R> void putDelayTask(Runnable runner, long delayMillSeconds) {
        if (runner == null) {
            throw new CreateTaskException("执行函数不能为空");
        }
        BaseJob<T, Object> defaultOnceJob = getOrCreateDefaultOnceJob(runner, delayMillSeconds);
        SingleTask singleTask = new SingleTask(defaultOnceJob, null, delayMillSeconds, null);
        defaultOnceJob.putTask(singleTask);
    }

    /**
     * 重载
     *
     * @param delayMillSeconds 延时多长时间执行 毫秒
     * @author wangjiang
     * @date 2020-11-16 11:51:25
     */
    public <T, R> void putDelayTask(ITaskProcessor<T, R> processor, long delayMillSeconds) {
        if (processor == null) {
            throw new CreateTaskException("执行对象不能为空");
        }
        BaseJob<T, R> defaultOnceJob = getOrCreateDefaultOnceJob(processor, delayMillSeconds);
        SingleTask singleTask = new SingleTask(defaultOnceJob, null, delayMillSeconds, null);
        defaultOnceJob.putTask(singleTask);
    }


    /**
     * 创建默认重试任务
     *
     * @param processor
     * @param delayMillSeconds
     * @param retryLimitMillSeconds
     * @param <T>
     * @author wangjiang
     * @date 2020-11-16 11:51:25
     */
    public <T, R> void putRetryTask(T t,
                                    ITaskProcessor<T, R> processor,
                                    long delayMillSeconds,
                                    long retryLimitMillSeconds) {
       putRetryTask(t, processor, null, delayMillSeconds, retryLimitMillSeconds);
    }

    /**
     * 创建默认重试任务
     *
     * @param processor
     * @param callback
     * @param delayMillSeconds
     * @param retryLimitMillSeconds
     * @param <T>
     * @author wangjiang
     * @date 2020-11-16 11:51:25
     */
    public <T, R> void putRetryTask(T t,
                                    ITaskProcessor<T, R> processor,
                                    Consumer<T> callback,
                                    long delayMillSeconds,
                                    long retryLimitMillSeconds) {
        if (processor == null) {
            throw new CreateTaskException("执行对象不能为空");
        }
        BaseJob<T, R> defaultRetryJob = getOrCreateDefaultRetryJob(processor, callback, delayMillSeconds, retryLimitMillSeconds);
        SingleTask singleTask = new SingleTask(defaultRetryJob, t, retryLimitMillSeconds, callback);
        defaultRetryJob.putTask(singleTask);
    }

    /**
     * 创建默认重试任务
     * @author wangjiang
     * @date 2020-11-16 01:52:42
     */
    private <T, R> BaseJob<T, R> getOrCreateDefaultRetryJob(ITaskProcessor<T,R> processor,
                                                         Consumer<T> callback,
                                                         long delayMillSeconds,
                                                         long retryLimitMillSeconds) {
        ScheduledJob<T, R> job = (ScheduledJob<T, R>) jobInfoMap.get(TaskConstant.DEFAULT_RETRY_SCHEDULED_JOB_NAME);
        if (null == job) {
            job = new ScheduledJob<>(retryLimitMillSeconds);
            job.setName(TaskConstant.DEFAULT_RETRY_SCHEDULED_JOB_NAME);
            job.setScheduleDelaySeconds(delayMillSeconds);
            job.setRunOnPut(true);
            job.setMonitor(monitorJob);
            job.setTaskExecutor(taskExecutor);
            job.setTaskProcessor(processor);
            job.setExpiredTime(defaultDelayJobExpireMillSecond); // 12s后过期
            registerJob(job);
        }
        return job;
    }

    /**
     * 获取默认延时执行任务
     *
     * @author wangjiang
     * @date 2020-11-16 11:55:48
     */
    private <T, R> BaseJob<T, R> getOrCreateDefaultOnceJob(ITaskProcessor<T, R> processor, long delaySeconds) {
        ScheduledJob<T, R> defaultDelayJob = (ScheduledJob<T, R>) jobInfoMap.get(TaskConstant.DEFAULT_ONCE_SCHEDULED_JOB_NAME);
        if (null == defaultDelayJob) {
            // 制作一个不重试延时执行job
            defaultDelayJob = new ScheduledJob(0);
            defaultDelayJob.setName(TaskConstant.DEFAULT_ONCE_SCHEDULED_JOB_NAME);
            defaultDelayJob.setMonitor(monitorJob);
            defaultDelayJob.setExpiredTime(defaultDelayJobExpireMillSecond); // 12s后过期
            defaultDelayJob.setTaskExecutor(taskExecutor);
            defaultDelayJob.setScheduleDelaySeconds(delaySeconds);
            defaultDelayJob.setRunOnPut(false);
            defaultDelayJob.setTaskProcessor(processor);
            registerJob(defaultDelayJob);
        }
        return defaultDelayJob;
    }

    /**
     * 获取默认延时执行任务
     *
     * @author wangjiang
     * @date 2020-11-16 11:55:48
     */
    private <T, R> BaseJob<T, R> getOrCreateDefaultOnceJob(Runnable runnable, long delaySeconds) {
        ScheduledJob<T, R> defaultDelayJob = (ScheduledJob<T, R>) jobInfoMap.get(TaskConstant.DEFAULT_ONCE_SCHEDULED_JOB_NAME);
        if (null == defaultDelayJob) {
            // 制作一个不重试延时执行job
            defaultDelayJob = new ScheduledJob(0);
            defaultDelayJob.setName(TaskConstant.DEFAULT_ONCE_SCHEDULED_JOB_NAME);
            defaultDelayJob.setMonitor(monitorJob);
            defaultDelayJob.setExpiredTime(defaultDelayJobExpireMillSecond); // 12s后过期
            defaultDelayJob.setTaskExecutor(taskExecutor);
            defaultDelayJob.setScheduleDelaySeconds(delaySeconds);
            defaultDelayJob.setRunOnPut(false);
            defaultDelayJob.setTaskProcessor(new ITaskProcessor<T, Boolean>() {
                @Override
                public TaskResult<Boolean> taskExecute(T data) {
                    // 直接把消费函数放进去做处理
                    runnable.run();
                    return new TaskResult(TaskResultType.Success, true);
                }
            });
            registerJob(defaultDelayJob);
        }
        return defaultDelayJob;
    }

    /**
     * 批量产生任务
     *
     * @param taskData
     */
    public <T, R> void putTasks(String jobName, List<T> taskData, Consumer<TaskResult<R>> callback) {
        BaseJob<T, R> jobInfo = getJob(jobName);
        jobInfo.putTasks(taskData, callback);
    }

    // 获取job
    private <T, R> BaseJob<T, R> getJob(String jobName) {
        BaseJob<T, R> jobInfo = (BaseJob<T, R>) jobInfoMap.get(jobName);
        if (null == jobInfo) {
            throw new RuntimeException(jobName + "是个非法任务。");
        }
        return jobInfo;
    }

    /**
     * 获取job列表
     *
     * @return
     */
    public ConcurrentHashMap<String, BaseJob<?, ?>> getJobInfoMap() {
        return jobInfoMap;
    }

    /**
     * 移除job
     *
     * @author wangjiang
     * @date 2020-11-13 09:32:17
     */
    public void removeJob(String jobName) {
        // 检查job是否重启了，如果重启则不作任何操作
        BaseJob job = getJob(jobName);
        if (!job.isDisabled()) {
            log.info("job is restart, don't remove it from jobpool!");
            return;
        }
        // 任务完成后的回调工作
        if (taskResultHandler != null) {
            taskResultHandler.afterJobFinished(jobName);
        }
        // 停止job的队列阻塞
        job.putTask(new StopTask());
        getJobInfoMap().remove(jobName);
    }


    // -------- get set ---------

    public long getDefaultDelayJobExpireMillSecond() {
        return defaultDelayJobExpireMillSecond;
    }

    public void setDefaultDelayJobExpireMillSecond(long defaultDelayJobExpireMillSecond) {
        this.defaultDelayJobExpireMillSecond = defaultDelayJobExpireMillSecond;
    }
}
