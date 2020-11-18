package com.wangjiang.devops.flyway.test.controller;

import com.wangjiang.devops.flyway.test.domain.user.entity.UserEntity;
import com.wangjiang.devops.flyway.test.domain.user.req.UserReq;
import com.wangjiang.devops.flyway.test.domain.user.service.UserService;
import com.wangjiang.devops.flyway.test.domain.user.vo.UserVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("")
    public List<UserVo> list () {
        return userService.getList();
    }

    @PostMapping("")
    public UserEntity create (
           @Validated @RequestBody UserReq userReq
    ) {
        return userService.create(userReq);
    }

}
