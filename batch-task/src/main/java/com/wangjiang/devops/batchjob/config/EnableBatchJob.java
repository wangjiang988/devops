package com.wangjiang.devops.batchjob.config;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 *
 * @author wangjiang
 * @date 2020-11-13 11:08:51
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({
        BatchJobScanConfig.class
})
public @interface EnableBatchJob {
}
