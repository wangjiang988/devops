package com.wangjiang.devops.batchjob;

import com.wangjiang.devops.batchjob.constant.TaskResultType;
import com.wangjiang.devops.batchjob.job.MonitorJob;
import com.wangjiang.devops.batchjob.job.ScheduledJob;
import com.wangjiang.devops.batchjob.task.ITaskProcessor;
import com.wangjiang.devops.batchjob.vo.TaskResult;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TestJobPool {

    public static void main(String[] args) throws InterruptedException {
        BatchJobPool jobPool = new BatchJobPool();

        // 创建任务
        ScheduledJob testJob = new ScheduledJob(2);
        testJob.setName("Test_Job");
        testJob.setExpiredTime(100);
        testJob.setScheduleDelaySeconds(3000); // 5s 执行一次
        testJob.setTaskProcessor(new ITaskProcessor<Integer, Integer>() {
            @Override
            public TaskResult<Integer> taskExecute(Integer start) {
                if (start > 100) {
                    return new TaskResult<>(TaskResultType.Success, start);
                } else {
                    start++;
                    System.out.println(Thread.currentThread().getName() + ",执行返回：" + start);
                    return new TaskResult<>(TaskResultType.Failure, start);
                }
            }
        });
        // 放入任务池
        jobPool.registerJob(testJob);
        // 像任务池中的该job中放入任务进行执行！
        List<Integer> taskData = Arrays.asList(1,2,3);
        jobPool.putTasks(testJob.getName(), taskData, ret -> {
            System.out.println(Thread.currentThread().getName() + ", 回调数据显示：" + ret);
        });
//        jobPool.putTask(testJob.getName(), 2, ret -> {
//            System.out.println(Thread.currentThread().getName() + ", 回调数据显示：" + ret);
//        });
//        jobPool.putTask(testJob.getName(), 3, ret -> {
//            System.out.println(Thread.currentThread().getName() + ", 回调数据显示：" + ret);
//        });


        TimeUnit.SECONDS.sleep(20);

    }

}
