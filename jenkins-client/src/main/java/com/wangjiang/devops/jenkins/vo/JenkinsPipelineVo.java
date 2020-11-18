package com.wangjiang.devops.jenkins.vo;

import lombok.Data;

import java.util.List;

/**
 *
 * @author wangjiang
 * @date 2020-11-09 11:54:23
 */
@Data
public class JenkinsPipelineVo {

    // pipeline 类型  gcc tomcat npm 等
    private String type;

    private List<PipeLineStage> pipeLineStages;


    /**
     * 封装 pipeline脚本
     * @author wangjiang
     * @date 2020-11-09 11:54:20
     */
    public String getPipelineScript () {
        String script = "node {\n%s\n} ";
        String nodeScripts = "";
        for (PipeLineStage stage: pipeLineStages) {
            String nodeScript = "    stage (\"%s\") {%s    }\n";
            String format = String.format(nodeScript, stage.getName(), stage.getScripts());
            nodeScripts += format;
        }
        script = String.format(script, nodeScripts);
        return script;
    }

}
