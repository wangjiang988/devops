package com.wangjiang.devops.flyway.test.domain.user.service;

import com.wangjiang.devops.flyway.test.domain.user.entity.UserEntity;
import com.wangjiang.devops.flyway.test.domain.user.repository.UserEntityRepository;
import com.wangjiang.devops.flyway.test.domain.user.req.UserReq;
import com.wangjiang.devops.flyway.test.domain.user.vo.UserVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserEntityRepository userEntityRepository;


    @Override
    public List<UserVo> getList() {
        List<UserEntity> all = Optional.of(userEntityRepository.findAll()).orElse(Collections.emptyList());
        return all.stream().map(r -> {
            UserVo userVo = new UserVo();
            BeanUtils.copyProperties(r, userVo);
            return userVo;
        }).collect(Collectors.toList());
    }



    @Override
    public UserEntity create(UserReq userReq) {
        UserEntity userEntity = new UserEntity();
        userEntity.setName(userReq.getName());
        UserEntity ret = userEntityRepository.save(userEntity);
        return ret;
    }

    @Override
    public void remove(UserEntity user) {
        userEntityRepository.delete(user);
    }


}
