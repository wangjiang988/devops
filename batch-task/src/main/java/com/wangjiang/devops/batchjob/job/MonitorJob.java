package com.wangjiang.devops.batchjob.job;

import com.wangjiang.devops.batchjob.BatchJobPool;
import com.wangjiang.devops.batchjob.vo.DelayVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.DelayQueue;

/**
 * 监控任务 这个任务最好用单例模式，只起一个去看
 * 监控任务池中的任务，没有过期时间
 * @author wangjiang
 * @date 2020-11-12 06:56:28
 */
public class MonitorJob implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(MonitorJob.class);

    private final BatchJobPool jobPool;

    private static final Logger logger = LoggerFactory.getLogger(MonitorJob.class);

    //存放已完成任务等待过期的队列
    private static DelayQueue<DelayVO<String>> queue
            = new DelayQueue<DelayVO<String>>();

    public MonitorJob(BatchJobPool jobPool) {
        this.jobPool = jobPool;
    }


    @Override
    public void run() {
        logger.debug("monitor thread is running");
        while(true) {
            try {
                //拿到已经过期的任务
                DelayVO<String> item = queue.take();
                String jobName =  item.getData();
                jobPool.removeJob(jobName);
                log.info("[{}] is out of date,remove from map!", jobName);
            } catch (InterruptedException e) {
                e.printStackTrace();
                log.error("remove job error: {}", e.getMessage());
            }
        }
    }

    public void registerEmptyJob(String jobName, long expireTime) {
        DelayVO<String> item = new DelayVO<String>(jobName, expireTime);
        queue.offer(item);
        log.info("Job【"+jobName+"】已经放入了过期检查缓存，过期时长："+expireTime);
    }

}
