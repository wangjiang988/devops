package com.wangjiang.devops.batchjob.job;

import com.wangjiang.devops.batchjob.task.SingleTask;
import com.wangjiang.devops.batchjob.task.StopTask;
import com.wangjiang.devops.batchjob.vo.DelayVO;
import com.wangjiang.devops.batchjob.vo.TaskResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

/**
 * 定时任务任务实体类
 * T 任务执行传入数据类型
 * R task执行结果类型
 *
 * @author wangjiang
 * @date 2020-11-12 11:25:03
 */
public class ScheduledJob<T, R> extends BaseJob<T, R> {

    private static final int defaultRetryNumber = 3;
    // 重试多少毫秒 1000 * 60 * 1 为1分钟
    private static final long defaultRetryTime = 1000 * 60 * 1;

    private static final Logger logger = LoggerFactory.getLogger(ScheduledJob.class);

    // 延迟执行时间 毫秒
    private long scheduleDelaySeconds;

    // 失败任务充重试次数
    private final int retryNumber;

    // 是否放进来就执行 默认为true
    private boolean runOnPut = true;

    // 存放已完成任务等待过期的队列
    private DelayQueue<DelayVO<SingleTask<?, ?>>> taskQueue
            = new DelayQueue<>();

    public ScheduledJob() {
        // 默认重试三次
        this.retryNumber = defaultRetryNumber;
        this.setTaskFailureRetryTime(0);
    }

    /**
     * 失败重试次数
     *
     * @author wangjiang
     * @date 2020-11-13 11:56:08
     */
    public ScheduledJob(int failureRetryNumber) {
        this.setTaskFailureRetryTime(0);
        this.retryNumber = failureRetryNumber;
    }

    /**
     * 任务失败重试过期时间
     *
     * @author wangjiang
     * @date 2020-11-13 03:56:44
     */
    public ScheduledJob(long failureRetryTime) {
        if (failureRetryTime <= 0) {
            this.setTaskFailureRetryTime(0);
            this.retryNumber = defaultRetryNumber;
        } else {
            this.setTaskFailureRetryTime(failureRetryTime);
            this.retryNumber = 0;
        }
    }


    /**
     * 放入延时队列
     * 所有执行任务的入口
     * @author wangjiang
     * @date 2020-11-13 05:00:55
     */
    @Override
    public void putTask0(SingleTask<T, R> task) {
        // 如果是停止线程任务，则不计数
        if (!(isStopTask(task))) {
            this.setLength(this.getLength() + 1);
            logger.debug("currentJob[{}] len:{}", this.getName(), getLength());
        }

        // 如果是第一次进来，判断是否立即执行, 不用放入延时队列
        if (task.getRunIndex() == 0 && !(isStopTask(task)) && runOnPut() ) {
            runTask(task);
        } else {
            taskQueue.offer(new DelayVO(task, scheduleDelaySeconds));
        }
    }

    @Override
    public void putTasks0(List<T> dataList, Consumer<TaskResult<R>> callback) {
        dataList.forEach(data -> {
            SingleTask<T, R> task = new SingleTask(this, data, getFailureActiveTime(), callback);
            putTask(task);
        });
    }

    /**
     * 执行任务
     *
     * @author wangjiang
     * @date 2020-11-12 05:24:01
     */
    @Override
    public void runTask(SingleTask<T, R> task) {
        if (task.getCallback() == null) {
            CompletableFuture.supplyAsync(task, getTaskExecutor());
        } else {
            CompletableFuture.supplyAsync(task, getTaskExecutor()).thenAccept(task.getCallback());
        }
    }

    @Override
    protected void handleFailureTask(SingleTask<T, R> task) {
        // 如果没有过期则继续放入该任务 继续执行！
        if (!hasExpired(task)) {
            putTask(task);
        } else {
            logger.info("失败次数或时间超过重试机制，任务丢弃");
        }
    }

    /**
     * 判断是否已经过期任务
     *
     * @author wangjiang
     * @date 2020-11-13 04:22:57
     */
    private boolean hasExpired(SingleTask<T, R> task) {
        if (retryNumber > 0) {
            return task.getRunIndex() > retryNumber;
        } else if (getFailureActiveTime() > 0) {
            long current = System.currentTimeMillis();//将传入的时长转换为超时的时刻
            return task.getActiveTime() < current;
        } else {
            // 如果两种判断方式都为0 不做过期重试，直接判定过期
            return true;
        }
    }

    @Override
    public void run0() {
        DelayVO<SingleTask<?, ?>> task = null;
        while (true) {
            logger.info("开始从job[{}]中取出任务", getName());
            try {
                task = taskQueue.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            logger.info("从job[{}]中取出任务", getName());
            // 这个意思是该线程任务需要结束了
            if (task.getData() instanceof StopTask) {
                logger.info("任务[{}]阻塞队列停止", getName());
                break;
            }
            runTask((SingleTask<T, R>) task.getData());
        }
    }


    public long getScheduleDelaySeconds() {
        return scheduleDelaySeconds;
    }

    public void setScheduleDelaySeconds(long scheduleDelaySeconds) {
        this.scheduleDelaySeconds = scheduleDelaySeconds;
    }


    // ---- getter setter


    public int getRetryNumber() {
        return retryNumber;
    }

    public boolean runOnPut() {
        return runOnPut;
    }

    public void setRunOnPut(boolean runOnPut) {
        this.runOnPut = runOnPut;
    }
}
