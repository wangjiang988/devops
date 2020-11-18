package com.wangjiang.devops.jenkins;

import com.offbytwo.jenkins.JenkinsServer;
import com.wangjiang.devops.jenkins.config.JenkinsConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class JenkinsServerHolder implements InitializingBean {

    private Map<String, JenkinsServer> clients = new ConcurrentHashMap<>();

    @Resource
    private JenkinsConfig jenkinsConfig;

    private JenkinsServer defaultJenkinsServer;

    /**
     * 项目类型 gcc
     */
    private static final String PROJECT_TYPE_GCC = "gcc";

    /**
     * 获取 JenkinsServer
     */
    public JenkinsServer getJenkinsServer(String envMark, String jenkinsUrl ,String type) {

        if ( StringUtils.isNotEmpty(jenkinsUrl) && PROJECT_TYPE_GCC.equals(type)) {

            if (clients.get(envMark) == null) {
                try {
                    clients.put(envMark, new JenkinsServer(new URI(jenkinsUrl), jenkinsConfig.getUsername(), jenkinsConfig.getPassword()));
                } catch (URISyntaxException e) {
                    log.error(envMark + "jenkinserver init fail", e);
                }
            }
            return clients.get(envMark);

        } else {
            return defaultJenkinsServer;
        }

    }

    @Override
    public void afterPropertiesSet() throws Exception {
        try {
            defaultJenkinsServer = new JenkinsServer(new URI(jenkinsConfig.getUrl()), jenkinsConfig.getUsername(), jenkinsConfig.getPassword());
        } catch (URISyntaxException e) {
            log.error("jenkinserver init fail", e);
        }
    }


}
