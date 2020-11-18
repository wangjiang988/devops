package com.wangjiang.devops.flyway.test.domain.user.repository;

import com.wangjiang.devops.flyway.test.domain.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;


public interface UserEntityRepository extends JpaRepository<UserEntity,
        String>,
        QuerydslPredicateExecutor<UserEntity> {

}
