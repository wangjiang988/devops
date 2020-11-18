package com.wangjiang.devops.batchjob.task;

/**
 * 任务结果处理类
 * @author wangjiang
 * @date 2020-11-12 01:12:29
 */
public interface TaskResultHandler {
    Boolean afterJobFinished(String jobName);
}
