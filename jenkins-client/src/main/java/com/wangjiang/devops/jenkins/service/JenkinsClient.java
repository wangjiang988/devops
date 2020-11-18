package com.wangjiang.devops.jenkins.service;

import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.model.Build;
import com.offbytwo.jenkins.model.JobWithDetails;
import com.wangjiang.devops.jenkins.intf.JenkinsLogHandler;

/**
 * @author zhangwei
 * @create 2018-05-03 9:55
 **/
public interface JenkinsClient {

    /**
     * 1.jobxml
     * 2.jenkins create
     *
     * @param jobXml
     * @param jobName
     */
    Long createJob(JenkinsServer jenkinsServer, String jobXml, String jobType, String jobName);


    Build getJobBuild(JenkinsServer jenkinsServer, String jobName, Integer buildNumber);


    /**
     * 结束jenkins构建
     *
     * @param jobName
     * @param buildNumber
     */
    void stopJob(JenkinsServer jenkinsServer, String jobName, Integer buildNumber);


    /**
     * jenkins删除job
     *
     * @param jobName
     */
    void deleteJob(JenkinsServer jenkinsServer, String jobName);

    /**
     * 结束jenkins构建
     *
     * @param queueItemUrl
     */
    void stopJob(JenkinsServer jenkinsServer, String queueItemUrl);

    /**
     * 获取构建日志
     *
     * @param jobName
     * @param number
     * @return
     */
    String getLog(JenkinsServer jenkinsServer, String jobName, Integer number);

    /**
     * tail jenkins 日志
     *
     * @param jobName
     * @param number
     * @param jenkinsLogHandler 日志结果处理器
     */
    void tailLog(JenkinsServer jenkinsServer, String jobName, Integer number, JenkinsLogHandler jenkinsLogHandler);

    /**
     * 检查当前job是否在编译中
     * @param jobName
     * @return
     */
    Boolean checkLastBuildIsCompiling(JenkinsServer jenkinsServer, String jobName);

    /**
     * 获取sonar测试报告
     *
     * @param jobName
     * @param number
     * @return
     */
    String getSonarReport(JenkinsServer jenkinsServer, String jobName, Integer number);

    /**
     * 获取 JobWithDetails
     *
     * @param jobName
     * @return
     */
    JobWithDetails getJobWithDetails(JenkinsServer jenkinsServer, String jobName);
}