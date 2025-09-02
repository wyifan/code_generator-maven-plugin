package com.yifan.code_generator_maven_plugin.model;

import lombok.Data;

@Data
public class ColumnDefinition {
    private String javaName;
    private String dbName;
    private String dbType;
    private String javaType;
    private String comment;
}
