package com.wangjiang.devops.batchjob.vo;

import com.wangjiang.devops.batchjob.constant.TaskResultType;
import com.wangjiang.devops.batchjob.task.SingleTask;

/**
 * 任务执行结果封装类
 * R 执行结果类型
 * @author wangjiang
 * @date 2020-11-12 11:33:02
 */
public class TaskResult<R> {

    //方法本身运行是否正确的结果类型
    private final TaskResultType resultType;
    //方法的业务结果数据；
    private final R returnValue;
    // 任务持有
    private SingleTask<?, R> task;
    // 这里放方法失败的原因
    private final String reason;

    public TaskResult(TaskResultType resultType, R returnValue, String reason) {
        super();
        this.resultType = resultType;
        this.returnValue = returnValue;
        this.reason = reason;
    }

    // 方便业务人员使用，这个构造方法表示业务方法执行成功返回的结果
    public TaskResult(TaskResultType resultType, R returnValue) {
        super();
        this.resultType = resultType;
        this.returnValue = returnValue;
        this.reason = "Success";
    }

    public TaskResultType getResultType() {
        return resultType;
    }

    public R getReturnValue() {
        return returnValue;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public String toString() {
        return "TaskResult [resultType=" + resultType
                + ", returnValue=" + returnValue
                + ", reason=" + reason + "]";
    }

}
