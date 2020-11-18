package com.wangjiang.devops.jenkins;

import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.model.Job;
import com.wangjiang.devops.jenkins.service.JenkinsClient;
import com.wangjiang.devops.jenkins.util.Dom4jUtil;
import com.wangjiang.devops.jenkins.vo.JenkinsPipelineVo;
import com.wangjiang.devops.jenkins.vo.PipeLineStage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

@RunWith(SpringRunner.class)
@ContextConfiguration({ "classpath:spring/applicationContext.xml" })
public class ClientTest {

    private static final String JOB_TYPE = "org.jenkinsci.plugins.workflow.job.WorkflowJob";

    @Autowired
    private JenkinsServerHolder jenkinsServerHolder;

    @Autowired
    private JenkinsClient jenkinsClient;

    @Test
    public void TestJobInfo() {
        JenkinsServer jenkinsServer = jenkinsServerHolder.getJenkinsServer("20", "192.168.33.20", null);
        try {
            Map<String, Job> jobs = jenkinsServer.getJobs();
            for (String k: jobs.keySet()) {
                Job job = jobs.get(k);
                System.out.println(k);
                System.out.println(job);
                System.out.println("=========");
                String jobXml = jenkinsServer.getJobXml(k);
                System.out.println(jobXml);
                System.out.println("=========");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Assert.notNull(jenkinsServer, "对象为空");
    }

    /**
     * 测试任务创建
     */
    @Test
    public void TestJobCreate() {
        
        JenkinsServer jenkinsServer = jenkinsServerHolder.getJenkinsServer("dev", "192.168.33.20", null);

        JenkinsPipelineVo pipelineVo = new JenkinsPipelineVo();
        // 流水线类型 1. tomcat 2. jdk 3. npm
        pipelineVo.setType("tomcat");
        //1. 拉取代码阶段
        PipeLineStage gitStage = new PipeLineStage("git stage");
        gitStage.appendScript("echo '==== git pull start ===='");
        gitStage.appendScript("echo '||'");
        gitStage.appendScript("git branch: 'master', credentialsId: '02550e58-1a5c-40d8-9d67-efd78eff2436', url: 'http://192.168.33.11:10080/root/ordev-svc.git'");
        gitStage.appendScript("env.check_to_tag='v1.0.0'");
        gitStage.appendScript("sh '[ -n \"${check_to_tag}\" ] &&  git checkout ${check_to_tag} ||  { echo -e \"切换至指定的tag的版本，tag：${check_to_tag} 不存在或为空，请检查输入的tag!\" && exit 111; }'");
        gitStage.appendScript("echo '||'");
        gitStage.appendScript("echo '==== git pull end   ===='");

        //2. mvn 打包阶段
        PipeLineStage mvnStage = new PipeLineStage("mvn build stage");
        mvnStage.appendScript("echo '==== mvn begin ===='");
        mvnStage.appendScript("echo '||'");
        mvnStage.appendShellScript("mvn clean package -Dmaven.test.skip=true  -Dmaven.javadoc.skip=true -Dmaven.compile.fork=true -P local -T 1C");
        mvnStage.appendScript("echo '||'");
        mvnStage.appendScript("echo '==== mvn end   ===='");

        //3. docker 镜像构建并推送到镜像库
        PipeLineStage dockerStage = new PipeLineStage("docker build and push stage");
        String imageVersion = "v1.0.0";
        dockerStage.appendScript("echo '==== docker build and push begin ===='");
        dockerStage.appendShellScript("mv ./order-api/target/*.jar ./docker/");
        dockerStage.appendShellScript("pwd");
        dockerStage.appendShellScript("cd ./docker && docker build -t 192.168.33.20/java-test/k8s-order-svc:" + imageVersion + " .");
        dockerStage.appendShellScript("pwd");
        dockerStage.appendShellScript("docker push 192.168.33.20/java-test/k8s-order-svc:" + imageVersion);
        dockerStage.appendScript("echo '==== docker build and push end   ===='");

        // 4. helm包制作并生成helm包并推送到仓库
        PipeLineStage helmStage = new PipeLineStage("helm stage");
        helmStage.appendScript("echo '==== helm stage begin ===='");
        helmStage.appendScript("echo 'processing...'");
        helmStage.appendScript("echo '==== helm stage end ===='");

        // 需要顺序放入
        pipelineVo.setPipeLineStages(Arrays.asList(gitStage, mvnStage, dockerStage, helmStage));

        String jobxml = Dom4jUtil.getJenkinsJobXml(pipelineVo);
        Long test_pipeline_v2 = jenkinsClient.createJob(jenkinsServer, jobxml, JOB_TYPE, "test_pipeline_v2");
        Assert.notNull(test_pipeline_v2, "对象制作失败");
    }

}
