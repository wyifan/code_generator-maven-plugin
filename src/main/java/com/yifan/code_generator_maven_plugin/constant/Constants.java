package com.yifan.code_generator_maven_plugin.constant;

import java.util.Set;

/**
 * 常量信息公共类
 */
public final class Constants {
    private Constants() {
    }

    public static final class TemplateConstants {
        public static final String FILE_TYPE_JAVA = "java";
        public static final String FILE_TYPE_RESOURCE = "resource";
    }

    public static final class ConfigModeConstant {
        public static final String OVERWRITE_MODE = "overwrite";
        public static final String APPEND_MODE = "append";
    }

    public enum MergeStrategy {
        KEEP_FIRST, KEEP_SECOND;
    }

    public static final class PackageSuffix {
        public static final String BASE = "base";
        public static final String COMMON = "common";
        public static final String UTILS = "utils";
        public static final String VO = "vo";
        public static final String DTO = "dto";
        public static final String SERVICE = "service";
        public static final String CONTROLLER = "controller";
        public static final String SERVICE_IMPL = "service.impl";
        public static final String MAPPER = "mapper";
        public static final String ENTITY = "entity";
        public static final String CONFIG = "config";
        public static final String HANDLER = "handler";
        public static final String INTERCEPTOR = "interceptor";
        public static final String EXCEPTION = "exception";
    }

    public static final class FileConstant {
        public static final String JAVA_RESOURCE_PATH = "src/main/java/";
        public static final String RESOURCE_PATH = "src/main/resources/";
        public static final String MAPPER_PATH = "src/main/resources/mapper/";
        public static final String TEMPLATE_PATH = "code_generator/templates/";
        public static final String CONFIG_PATH = "code_generator/generator_setting.yml";
        // Constants for SQL script file paths and names
        public static final String SQL_SCRIPT_PATH_DIRECTORY = "src/main/resources/sql";
        public static final String SQL_SCRIPT_FILE_NAME = "schema.sql";

    }

    public static final class ColumnConstants {
        public static final String TABLE_TYPE = "TABLE"; // Constant for DatabaseMetaData.getTables type filter
        public static final String COLUMN_TYPE_NAME = "TYPE_NAME"; // Column name for ResultSet
        public static final String COLUMN_NAME = "COLUMN_NAME"; // Column name for ResultSet
        public static final String COLUMN_REMARKS = "REMARKS"; // Column name for ResultSet
        /**
         * A static final Set to store all base column names (converted to lowercase).
         * Benefits:
         * 1. Efficient lookup: Set's contains method has an average time complexity of O(1).
         * 2. Easy maintenance: Modifications to base fields only require changing this set.
         * 3. Clean code: The logic for isBaseColumn method becomes very simple.
         */
        public static final Set<String> BASE_COLUMN_NAMES = Set.of(
                "id",
                "created_by",
                "created_by_name",
                "create_time",
                "updated_by",
                "updated_by_name",
                "updated_time",
                "deleted",
                "version");
    }

}
