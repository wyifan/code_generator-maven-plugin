package com.yifan.code_generator_maven_plugin.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TableConfig {
    private String tableName;
    private String entityName;
    private boolean useBaseEntity;
    private List<ColumnDefinition> columns = new ArrayList<>();
}
