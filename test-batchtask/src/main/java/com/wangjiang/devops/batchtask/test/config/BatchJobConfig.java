package com.wangjiang.devops.batchtask.test.config;

import com.wangjiang.devops.batchjob.BatchJobPool;
import com.wangjiang.devops.batchjob.config.BatchJobConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 重新定义线程池
 * @author wangjiang
 * @date 2020-11-13 11:31:39
 */
@Configuration
public class BatchJobConfig implements BatchJobConfigurer {

    @Bean
    @Override
    public BatchJobPool initJobPool() {
        return new BatchJobPool(null);
    }
}
