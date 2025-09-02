package com.yifan.code_generator_maven_plugin.model;

import lombok.Data;

@Data
public class SwaggerConfig {
    private boolean enableSwagger;
    private String swaggerTitle;
    private String swaggerVersion;
    private String swaggerDescription;
}
