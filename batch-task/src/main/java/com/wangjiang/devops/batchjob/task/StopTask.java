package com.wangjiang.devops.batchjob.task;


/**
 * 停止任务线程
 *
 * @author wangjiang
 * @date 2020-11-12 12:38:04
 */
public final class StopTask extends SingleTask {

    public StopTask() {
        super(null, "stop", 0,null);
    }
}
