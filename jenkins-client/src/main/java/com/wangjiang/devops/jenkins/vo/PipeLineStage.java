package com.wangjiang.devops.jenkins.vo;

import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * pipeline 阶段元素
 * @author wangjiang
 * @date 2020-11-09 11:48:52
 */
public class PipeLineStage {
    // 阶段
    private String name;

    private List<String> scripts;

    public PipeLineStage (String name) {
        this.scripts = new ArrayList<>();
        this.name = name;
    }

    public void appendScript(String script) {
        this.scripts.add(script);
    }

    public void appendShellScript(String shell) {
        this.scripts.add(String.format("sh '%s'", shell));
    }

    public String getScripts() {
        String ret =  "\n";
        if (CollectionUtils.isNotEmpty(scripts)) {
            for (String script : scripts) {
                // 8个空格
                ret += "        " + script + "\n";
            }
        }
        return ret;
    }

    public String getName() {
        return name;
    }


}
