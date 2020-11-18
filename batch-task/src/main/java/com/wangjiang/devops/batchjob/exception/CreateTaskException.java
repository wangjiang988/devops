package com.wangjiang.devops.batchjob.exception;

/**
 * 创建task异常
 * @author wangjiang
 * @date 2020-11-12 11:56:12
 */
public class CreateTaskException extends RuntimeException {
    public CreateTaskException(String error_create_taskInfo) {
        super(error_create_taskInfo);
    }
}
