package com.yifan.code_generator_maven_plugin.model;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class PluginConfig {
    private String groupId;
    private String artifactId;
    private String version;
    private Map<String, Object> configuration;
    private List<Map<String, Object>> executions;
}
