package com.yifan.code_generator_maven_plugin.model;

import lombok.Data;

@Data
public class JdbcConfig {
    private String driver;
    private String url;
    private String username;
    private String password;
}
