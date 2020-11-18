package com.wangjiang.devops.flyway.test.domain.user.service;


import com.wangjiang.devops.flyway.test.domain.user.entity.UserEntity;
import com.wangjiang.devops.flyway.test.domain.user.req.UserReq;
import com.wangjiang.devops.flyway.test.domain.user.vo.UserVo;

import java.util.List;

public interface UserService {
    List<UserVo> getList();

    UserEntity create(UserReq userReq);

    void remove(UserEntity user);
}
