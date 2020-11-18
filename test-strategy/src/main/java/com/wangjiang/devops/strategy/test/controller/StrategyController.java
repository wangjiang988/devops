package com.wangjiang.devops.strategy.test.controller;

import com.wangjiang.devops.strategy.test.strategy.CalculatelOperationContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StrategyController {

  @Autowired
  private CalculatelOperationContext calculatelOperationContext;

  @RequestMapping(value = "/operation")
  public String strategySelect(@RequestParam("mode") String mode) {
    return String.valueOf(calculatelOperationContext.strategySelect(mode).calc(20, 5));
  }
}