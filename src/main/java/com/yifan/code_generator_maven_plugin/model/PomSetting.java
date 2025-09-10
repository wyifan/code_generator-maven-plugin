package com.yifan.code_generator_maven_plugin.model;

import com.yifan.code_generator_maven_plugin.constant.Constants;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class PomSetting {
    private String dependencyMode = Constants.ConfigModeConstant.OVERWRITE_MODE; // Added
    private Map<String, String> properties;
    private List<DependencyInfo> dependencies = new ArrayList<>();
    private String pluginMode = Constants.ConfigModeConstant.OVERWRITE_MODE; // Added
    private List<PluginConfig> buildPlugins = new ArrayList<>();
    private List<ResourceConfig> resourceConfig;
}
