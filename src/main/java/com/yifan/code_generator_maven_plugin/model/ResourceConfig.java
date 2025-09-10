package com.yifan.code_generator_maven_plugin.model;

import lombok.Data;

import java.util.List;

@Data
public class ResourceConfig {
    private String directory;
    private List<String> excludes;
}
