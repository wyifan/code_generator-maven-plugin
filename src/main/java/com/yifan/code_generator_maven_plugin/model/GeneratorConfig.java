package com.yifan.code_generator_maven_plugin.model;

import lombok.Data;

@Data
public class GeneratorConfig {
    private BaseConfig baseConfigs;
    private ModelSetting modelSettings;
    private PomSetting pomSettings;
    private SwaggerConfig swagger;
}
