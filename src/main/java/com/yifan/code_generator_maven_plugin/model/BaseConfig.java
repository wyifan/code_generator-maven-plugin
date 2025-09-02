package com.yifan.code_generator_maven_plugin.model;

import lombok.Data;

import java.util.Map;

@Data
public class BaseConfig {
    /**
     * 包名的前缀，比如：package com.yifan.code_generator.constant; 前缀配置就是com.yifan，后面加上项目名称，最后加上后缀
     */
    private String basePackagePrefix;
    private JdbcConfig jdbcConfig;
    private Map<String, String> typeMapping;
}
