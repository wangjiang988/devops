package com.wangjiang.devops.batchjob.job;

import com.wangjiang.devops.batchjob.constant.TaskResultType;
import com.wangjiang.devops.batchjob.task.ITaskProcessor;
import com.wangjiang.devops.batchjob.task.SingleTask;
import com.wangjiang.devops.batchjob.task.StopTask;
import com.wangjiang.devops.batchjob.vo.TaskResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * 任务执行类
 * 本身就是一个线程类，所以一些属性用基础属性即可
 *
 * @author wangjiang
 * @date 2020-11-12 05:28:50
 */
public abstract class BaseJob<T, R> implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(BaseJob.class);

    // 任务名 #wangjiang 2020-11-12 11:30:30#
    private String name;

    // 任务执行完后的多长时间过期 单位毫秒
    private long expiredTime;

    // 任务失败重试时长
    private long taskFailureRetryTime;

    private int length = 0;

    // 任务是否可用
    private boolean disabled = false;

    // 执行线程池
    private ExecutorService taskExecutor;

    // 执行任务类
    private ITaskProcessor<?, ?> taskProcessor;

    // 成功处理的任务数 #wangjiang 2020-11-12 11:30:24#
    private AtomicInteger successCount = new AtomicInteger(0);

    // 已处理的任务数 #wangjiang 2020-11-12 11:30:24#
    private AtomicInteger taskProcessorCount = new AtomicInteger(0);

    // 失败处理的任务数量 #wangjiang 2020-11-12 11:35:43#
    private AtomicInteger failCount = new AtomicInteger(0);

    // 结果队列，拿结果从头拿，放结果从尾部放, 便于查询某个job的所有执行结果
    private LinkedBlockingDeque<TaskResult<R>> taskDetailQueue = new LinkedBlockingDeque<>(100);
    // 监控任务
    private MonitorJob monitor;

    @Override
    public void run() {
        this.run0();
    }

    /**
     * 需要子类去实现
     *
     * @author wangjiang
     * @date 2020-11-12 05:30:23
     */
    protected abstract void run0();

    // 放入任务
    public abstract void putTask0(SingleTask<T, R> task);

    // 批量放入任务
    public abstract void putTasks0(List<T> taskData, Consumer<TaskResult<R>> callback);

    // 执行任务
    public abstract void runTask(SingleTask<T, R> task);

    // 处理失败任务
    protected abstract void handleFailureTask(SingleTask<T, R> task);

    protected boolean isStopTask(SingleTask task) {
        return task instanceof StopTask;
    }

    /**
     * 执行结果放入队列
     *
     * @author wangjiang
     * @date 2020-11-12 01:18:25
     */
    public void addTaskResult(TaskResult<R> result, SingleTask<T, R> task) {
        // 执行成功
        if (TaskResultType.Success.equals(result.getResultType())) {
            successCount.incrementAndGet();
            // 执行失败
        } else if (TaskResultType.Failure.equals(result.getResultType())) {
            failCount.incrementAndGet();
            //失败任务处理 具体类实现重试机制。
            handleFailureTask(task);
        } else {
            log.error("未知结果类型");
        }

        taskDetailQueue.addLast(result);
        taskProcessorCount.incrementAndGet();
        // 如果是批量任务，且执行结果数量等于任务长度，那么任务结束, 任务放入延时队列 等待一段时间后删除任务。
        if (taskProcessorCount.get() == length) {
            log.info("当前阶段任务执行结束: {}", this.getName());
            disableJob();
            monitor.registerEmptyJob(this.getName(), this.getExpiredTime());
        }
    }

    /**
     * 设置当前job为不可用
     *
     * @author wangjiang
     * @date 2020-11-13 12:00:04
     */
    private void disableJob() {
        this.disabled = true;
    }

    private void restartJob() {
        log.info("restart job [{}]", this.getName());
        this.disabled = false;
    }

    public boolean isDisabled() {
        return this.disabled;
    }

    public void putTask(SingleTask<T, R> task) {
        if (this.disabled && !isStopTask(task)) {
            log.info("current job [{}] is disabled because job is finised!", this.getName());
            restartJob();
        }
        this.putTask0(task);
    }

    public void putTasks(List<T> taskData, Consumer<TaskResult<R>> callback) {
        if (this.disabled) {
            log.info("current job [{}] is disabled because job is finised!", this.getName());
            restartJob();
        }
        this.putTasks0(taskData, callback);
    }


    // --------- blows is getters setters ---------

    public long getFailureActiveTime() {
        return this.taskFailureRetryTime;
    }

    public void setTaskFailureRetryTime(long taskFailureRetryTime) {
        this.taskFailureRetryTime = taskFailureRetryTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LinkedBlockingDeque<TaskResult<R>> getTaskDetailQueue() {
        return taskDetailQueue;
    }

    public void setTaskDetailQueue(LinkedBlockingDeque<TaskResult<R>> taskDetailQueue) {
        this.taskDetailQueue = taskDetailQueue;
    }

    public ITaskProcessor<?, ?> getTaskProcessor() {
        return taskProcessor;
    }

    public void setTaskProcessor(ITaskProcessor<?, ?> taskProcessor) {
        this.taskProcessor = taskProcessor;
    }


    public long getExpiredTime() {
        return expiredTime;
    }

    public void setExpiredTime(long expiredTime) {
        this.expiredTime = expiredTime;
    }


    public ExecutorService getTaskExecutor() {
        return taskExecutor;
    }

    public void setTaskExecutor(ExecutorService taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public MonitorJob getMonitor() {
        return monitor;
    }

    public void setMonitor(MonitorJob monitor) {
        this.monitor = monitor;
    }
}