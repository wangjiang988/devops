package com.wangjiang.devops.batchjob.config;

import com.wangjiang.devops.batchjob.BatchJobPool;

/**
 *
 * @author wangjiang
 * @date 2020-11-13 11:08:25
 */

public interface BatchJobConfigurer{
    BatchJobPool initJobPool();
}
