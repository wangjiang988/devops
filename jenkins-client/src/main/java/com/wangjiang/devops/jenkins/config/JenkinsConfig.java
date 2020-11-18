package com.wangjiang.devops.jenkins.config;

import lombok.Data;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author mr.dzf
 * @create 2018-03-07 9:17
 **/
@Data
@Component
//@ConfigurationProperties(
//        prefix = "jenkins"
//)
public class JenkinsConfig implements InitializingBean{

    /**
     * 地址
     */
    private String url;
    /**
     * 用户名
     */
    private String username;

    /**
     * 密钥地址
     */
    private String keyPath;

    /***
     * 密码
     */
    private String password;

    /**
     * 通知状态地址
     */
    private String notifyUrl;
    /**
     * 前端项目npm打包镜像地址
     */
    private String registry;

    @Override
    public void afterPropertiesSet() throws Exception {
//        File file = new File(keyPath);
//        if(!file.exists()){
//            throw new FileNotFoundException("jenkins密钥未找到");
//        }
//        String key = FileUtils.readFileToString(file, StandardCharsets.UTF_8).replace("\n", "");
//        password = key;
    }
}