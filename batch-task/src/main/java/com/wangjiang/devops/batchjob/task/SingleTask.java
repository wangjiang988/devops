package com.wangjiang.devops.batchjob.task;

import com.wangjiang.devops.batchjob.job.BaseJob;
import com.wangjiang.devops.batchjob.vo.TaskResult;
import com.wangjiang.devops.batchjob.constant.TaskResultType;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 线程任务
 *
 * @author wangjiang
 * @date 2020-11-12 12:38:04
 */
public class SingleTask<T, R> implements Supplier<TaskResult<R>> {

    private final String id;
    // 任务详情
    private final BaseJob<T, R> jobInfo;
    // 任务执行数据
    private final T data;
    // 执行次数
    private int runIndex;
    // 执行到期时间
    private long activeTime;

    private final Consumer<TaskResult<R>> callback;

    public SingleTask(BaseJob<T, R> jobInfo,
                      T data,
                      long failureActiveTime,
                      Consumer<TaskResult<R>> callback) {
        this.data = data;
        this.jobInfo = jobInfo;
        this.runIndex = 0;
        this.callback = callback;
        // 如果过期时间小于0 或为空则参数无效
        if (Objects.isNull(failureActiveTime) || failureActiveTime <= 0) {
            this.activeTime = 0;
        } else {
            this.activeTime = failureActiveTime + System.currentTimeMillis();//将传入的时长转换为超时的时刻
        }
        this.id = UUID.randomUUID().toString();
    }

    public String getId() {
        return id;
    }

    public T getData() {
        return data;
    }

    public BaseJob<T, R> getJobInfo() {
        return jobInfo;
    }

    public int getRunIndex() {
        return runIndex;
    }

    public Consumer<TaskResult<R>> getCallback() {
        return callback;
    }

    @Override
    public TaskResult<R> get() {
        runIndex++;
        R r = null;
        ITaskProcessor<T, R> taskProcesser =
                (ITaskProcessor<T, R>) jobInfo.getTaskProcessor();
        // 执行结果
        TaskResult<R> result = null;

        try {
            result = taskProcesser.taskExecute(data);

            //要做检查，防止开发人员处理不当
            if (result == null) {
                result = new TaskResult<R>(TaskResultType.Exception, r,
                        "result is null");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result = new TaskResult<R>(TaskResultType.Exception, r,
                    e.getMessage());
        } finally {
            jobInfo.addTaskResult(result, this);
        }

        return result;
    }


    public long getActiveTime() {
        return activeTime;
    }
}
