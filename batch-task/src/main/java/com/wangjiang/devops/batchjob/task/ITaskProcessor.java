package com.wangjiang.devops.batchjob.task;

import com.wangjiang.devops.batchjob.vo.TaskResult;

/**
 * 单任务执行类
 * T 任务执行传入数据, R 任务执行返回结果类型
 * @author wangjiang
 * @date 2020-11-12 11:31:22
 */
public interface ITaskProcessor<T, R> {

    TaskResult<R> taskExecute(T data);

}
