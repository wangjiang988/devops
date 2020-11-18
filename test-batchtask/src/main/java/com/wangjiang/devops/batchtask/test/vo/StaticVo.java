package com.wangjiang.devops.batchtask.test.vo;

/**
 * 测试类
 * @author wangjiang
 * @date 2020-11-17 09:22:58
 */
public class StaticVo {
    public static volatile Integer index = 100;

    public static void reset() {
        index = 100;
    }
}
