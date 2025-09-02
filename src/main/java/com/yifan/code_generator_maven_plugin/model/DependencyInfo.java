package com.yifan.code_generator_maven_plugin.model;

import lombok.Data;

@Data
public class DependencyInfo {
    private String groupId;
    private String artifactId;
    private String version;
    private String scope;
}
