package com.wangjiang.devops.jenkins.util;

import com.wangjiang.devops.jenkins.vo.JenkinsPipelineVo;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;


@Slf4j
public class Dom4jUtil {

    private static SAXReader reader = new SAXReader();

    private static  final String JOB_CLASS_MAVEN = "hudson.maven.MavenModuleSet";

    private static  final String JOB_CLASS_FREE = "hudson.model.FreeStyleProject";

    private static final String XML_PATH = "/xml.templates";

    public static synchronized String getJenkinsJobXml(JenkinsPipelineVo jenkinsJobVO){
        try {
            String jobxmlPath = String.format("%s/%s", XML_PATH, "defaultJob.xml");;
            String path = Dom4jUtil.class.getResource(jobxmlPath).toString();

            Document document = reader.read(Dom4jUtil.class.getResource(jobxmlPath).getFile());

            // 获取pipeline节点
            Element rootElem = document.getRootElement();
            Element pipelineScript = rootElem.element("definition")
                    .element("script");

            pipelineScript.setText(jenkinsJobVO.getPipelineScript());

            return document.asXML();
        } catch (DocumentException e) {
            log.error("getJenkinsJobXml exception",e);
        }
        return null;
    }

}