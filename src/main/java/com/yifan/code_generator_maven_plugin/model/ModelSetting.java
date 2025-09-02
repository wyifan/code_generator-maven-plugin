package com.yifan.code_generator_maven_plugin.model;

import com.yifan.code_generator_maven_plugin.constant.Constants;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ModelSetting {
    private String tableAppendMode = Constants.ConfigModeConstant.OVERWRITE_MODE;
    private List<TableConfig> tables = new ArrayList<>();
    private String templateAppendMode = Constants.ConfigModeConstant.OVERWRITE_MODE; // Added
    private String templateDir; // Added
    private boolean isPlugin;
    private List<TemplateConfig> templates = new ArrayList<>();
}
