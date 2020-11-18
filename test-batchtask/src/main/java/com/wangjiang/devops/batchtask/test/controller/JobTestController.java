package com.wangjiang.devops.batchtask.test.controller;

import com.wangjiang.devops.batchjob.BatchJobPool;
import com.wangjiang.devops.batchjob.constant.TaskResultType;
import com.wangjiang.devops.batchjob.job.ScheduledJob;
import com.wangjiang.devops.batchjob.task.ITaskProcessor;
import com.wangjiang.devops.batchjob.vo.TaskResult;
import com.wangjiang.devops.batchtask.test.vo.StaticVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
public class JobTestController {
    private static final Logger log = LoggerFactory.getLogger(JobTestController.class);

    @Autowired
    private BatchJobPool batchJobPool;



    @RequestMapping(value = "/addJob")
    public String addJob() {
        // 创建任务 错误尝试10s过期
        ScheduledJob testJob = new ScheduledJob(10000l);
        testJob.setName("Test_Job");
        testJob.setExpiredTime(6000);
        testJob.setScheduleDelaySeconds(5000); // 5s 执行一次
        testJob.setTaskProcessor(new ITaskProcessor<Integer, Integer>() {
            @Override
            public TaskResult<Integer> taskExecute(Integer start) {
                if (start > 100) {
                    return new TaskResult<>(TaskResultType.Success, start);
                } else {
                    log.info("执行返回：" + start);
                    return new TaskResult<>(TaskResultType.Failure, start);
                }
            }
        });
        // 放入任务池
        batchJobPool.registerJob(testJob);
        // 像任务池中的该job中放入任务进行执行！
        List<Integer> taskData = Arrays.asList(1, 2, 3);
        batchJobPool.putTasks(testJob.getName(), taskData, ret -> {
            log.info("回调数据显示：" + ret);
        });

        return null;
    }

    @RequestMapping(value = "/putTask")
    public String putTask() {
        // 像任务池中的该job中放入任务进行执行！
        List<Integer> taskData = Arrays.asList(4, 5, 6);
        batchJobPool.putTasks("Test_Job", taskData, ret -> {
            log.info("回调数据显示：" + ret);
        });
        return null;
    }

    @RequestMapping(value = "/putOnceTask")
    public String putOnceTask() {
        // 像任务池中的该job中放入任务进行执行！
        // 1分钟后执行改任务
        String test = "test";
        batchJobPool.putDelayTask(() -> {
            log.info("10s后执行！:{}", test);
        }, 10000);

        batchJobPool.putDelayTask(new ITaskProcessor<String, String>() {

            @Override
            public TaskResult<String> taskExecute(String data) {
                log.info("10s后执行!");
                return new TaskResult(TaskResultType.Success, true);
            }
        }, 10000);
        return null;
    }

    @RequestMapping(value = "/putRetryTask")
    public String putRetryTask() {

        batchJobPool.putRetryTask(100, new ITaskProcessor<Integer, Boolean>() {
                    @Override
                    public TaskResult<Boolean> taskExecute(Integer data) {
                        log.info("data: {}", data);
                        log.info("staticVo.index: {}", StaticVo.index);
                        if ((data + StaticVo.index) > 210) {
                            StaticVo.reset();
                            return new TaskResult<>(TaskResultType.Success, true);
                        } else {
                            StaticVo.index++;
                            return new TaskResult<>(TaskResultType.Failure, false);
                        }
                    }
                },
                2000, // 2s 执行一次
                10000); // 重试10s钟
        return null;
    }

    @RequestMapping(value = "/monitor")
    public String monitor() {
        // 像任务池中的该job中放入任务进行执行！
        List<Integer> taskData = Arrays.asList(4, 5, 6);
        batchJobPool.putTasks("Test_Job", taskData, ret -> {
            log.info("回调数据显示：" + ret);
        });
        return null;
    }
}