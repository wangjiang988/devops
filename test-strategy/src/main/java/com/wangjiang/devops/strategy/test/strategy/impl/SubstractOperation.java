package com.wangjiang.devops.strategy.test.strategy.impl;

import com.wangjiang.devops.strategy.test.strategy.CalculateStrategy;
import org.springframework.stereotype.Component;

@Component
public class SubstractOperation implements CalculateStrategy {
  @Override
  public int calc(int num1, int num2) {
    return num1 - num2;
  }
}