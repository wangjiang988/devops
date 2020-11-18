package com.wangjiang.devops.batchjob.vo;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * 延时对象
 * @author wangjiang
 * @data 2020-11-12 04:06:41
 */
public class DelayVO<T> implements Delayed {

    private long activeTime; //到期时间，单位毫秒
    private T data;

    // activeTime是个过期时长
    public DelayVO(T data, long activeTime) {
        super();
        this.activeTime = TimeUnit.NANOSECONDS.convert(activeTime,
                                    TimeUnit.MILLISECONDS) + System.nanoTime();//将传入的时长转换为超时的时刻
        this.data = data;
    }

    public T getData() {
        return data;
    }

    //按照剩余时间排序
    @Override
    public int compareTo(Delayed o) {
        long d = getDelay(TimeUnit.NANOSECONDS)-o.getDelay(TimeUnit.NANOSECONDS);
        return (d == 0) ? 0 : ((d > 0) ? 1 : -1);
    }

    //返回元素的剩余时间
    @Override
    public long getDelay(TimeUnit unit) {
        long d = unit.convert(this.activeTime-System.nanoTime(),
                TimeUnit.NANOSECONDS);
        return d;
    }
}
