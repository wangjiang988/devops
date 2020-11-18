package com.wangjiang.devops.jenkins.intf;

import com.offbytwo.jenkins.model.BuildResult;

public interface JenkinsLogHandler {

    /**
     * 处理追踪日志
     *
     * @param log
     */
    void handle(String log);

    /**
     * jenkins 脚本完成后处理操作
     *
     * @param jobName
     * @param buildNumber
     * @param result
     */
    void finished(String jobName, Integer buildNumber, BuildResult result);

}
