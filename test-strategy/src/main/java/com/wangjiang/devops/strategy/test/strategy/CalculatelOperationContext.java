package com.wangjiang.devops.strategy.test.strategy;

import org.assertj.core.util.Preconditions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CalculatelOperationContext {

//  @Autowired
//  private Map<String, CalculateStrategy> strategyMap;

    private final Map<String, CalculateStrategy> strategyMap = new ConcurrentHashMap<>();

    @Autowired
    public void stragegyInteface(Map<String, CalculateStrategy> strategyMap) {
        this.strategyMap.clear();
        strategyMap.forEach(this.strategyMap::put);
        System.out.println(this.strategyMap);
    }

    /**
     * list map 都可以注入
     * @param mode
     * @return
     */
//    @Autowired
//    public void stragegyInteface2(List<CalculateStrategy> strategyMap) {
//        strategyMap.forEach(System.out::println);
//    }

    public CalculateStrategy strategySelect(String mode) {
        Preconditions.checkArgument(!StringUtils.isEmpty(mode), "不允许输入空字符串");
        return this.strategyMap.get(mode);
    }
}