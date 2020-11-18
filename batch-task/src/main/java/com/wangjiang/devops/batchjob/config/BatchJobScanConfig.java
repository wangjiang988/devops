package com.wangjiang.devops.batchjob.config;

import com.wangjiang.devops.batchjob.BatchJobPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;

@Configuration
@ComponentScan("com.wangjiang.devops.batchjob")
public class BatchJobScanConfig {
    private static final Logger log = LoggerFactory.getLogger(BatchJobScanConfig.class);

    @Autowired
    private BatchJobPool batchJobPool;


    @PreDestroy
    public void shutdownTaskExecutor() {
        log.info("关闭线程池");
        batchJobPool.getTaskExecutor().shutdown();
    }
}
