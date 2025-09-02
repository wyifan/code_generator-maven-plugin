package com.yifan.code_generator_maven_plugin.utils;


import com.yifan.code_generator_maven_plugin.model.GeneratorConfig;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @Description: Utility class for updating the application.yml file in the
 *               target project.
 */
public class ApplicationYmlUpdater { // Renamed from ApplicationYmlUtils

    // --- YAML Key Constants ---
    private static final String SPRING_KEY = "spring";
    private static final String DATASOURCE_KEY = "datasource";
    private static final String URL_KEY = "url";
    private static final String USERNAME_KEY = "username";
    private static final String PASSWORD_KEY = "password";
    private static final String DRIVER_CLASS_NAME_KEY = "driver-class-name";

    private static final String PROFILES_KEY = "profiles";
    private static final String ACTIVE_KEY = "active";
    private static final String DEV_PROFILE_VALUE = "dev";

    private static final String MYBATIS_PLUS_KEY = "mybatis-plus";
    private static final String MAPPER_LOCATIONS_KEY = "mapper-locations";
    private static final String MAPPER_LOCATION_VALUE = "classpath*:/mapper/**/*.xml";
    private static final String CONFIGURATION_KEY = "configuration";
    private static final String LOG_IMPL_KEY = "log-impl";
    private static final String LOG_IMPL_VALUE = "org.apache.ibatis.logging.stdout.StdOutImpl";
    // ADDED: New constants for MyBatis Plus global-config
    private static final String GLOBAL_CONFIG_KEY = "global-config";
    private static final String DB_CONFIG_KEY = "db-config";
    private static final String LOGIC_DELETE_FIELD_KEY = "logic-delete-field";
    private static final String LOGIC_DELETE_FIELD_VALUE = "deleted";
    private static final String LOGIC_DELETE_VALUE_KEY = "logic-delete-value";
    private static final Integer LOGIC_DELETE_VALUE = 1;
    private static final String LOGIC_NOT_DELETE_VALUE_KEY = "logic-not-delete-value";
    private static final Integer LOGIC_NOT_DELETE_VALUE = 0;

    private static final String SWAGGER_KEY = "swagger";
    private static final String AUTH_KEY = "auth";
    private static final String ADMIN_VALUE = "admin"; // Used for both username and password

    private final MavenProject project;
    private final Log log;
    private final File ymlFile;

    public ApplicationYmlUpdater(MavenProject project, Log log) { // Renamed constructor
        this.project = project;
        this.log = log;
        this.ymlFile = new File(project.getBasedir(), "src/main/resources/application.yml");
    }

    /**
     * Updates the application.yml file based on the provided generator
     * configuration.
     * This method will create the file if it does not exist.
     *
     * @param generatorConfig The configuration containing JDBC settings.
     * @throws IOException If an I/O error occurs during file operations.
     */
    @SuppressWarnings("unchecked")
    public void updateApplicationYml(GeneratorConfig generatorConfig) throws IOException {
        // Ensure application.yml file and its parent directories exist
        if (!ymlFile.exists()) {
            log.info("application.yml not found, creating a new one.");
            ymlFile.getParentFile().mkdirs();
            ymlFile.createNewFile();
        }

        log.info("Updating application.yml...");

        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        options.setIndent(2);
        Yaml yaml = new Yaml(options);

        Map<String, Object> ymlMap = new LinkedHashMap<>();
        // Load existing YAML content
        try (FileReader reader = new FileReader(ymlFile)) {
            Map<String, Object> loadedMap = yaml.load(reader);
            if (loadedMap != null) {
                ymlMap.putAll(loadedMap);
            }
        }

        boolean modified = false;

        // 1. Process database configuration
        if (generatorConfig != null && generatorConfig.getBaseConfigs().getJdbcConfig() != null
                && updateJdbcConfiguration(ymlMap, generatorConfig)) {
            modified = true;
        }

        // 2. Process Spring Profile
        if (updateSpringProfiles(ymlMap)) {
            modified = true;
        }

        // 3. Process MyBatis Plus Configuration
        if (updateMybatisPlusConfiguration(ymlMap)) {
            modified = true;
        }

        // 4. Process Swagger Authentication Configuration
        if (updateSwaggerConfiguration(ymlMap)) {
            modified = true;
        }

        // 5. Write back to file if any modification occurred
        if (modified) {
            try (FileWriter writer = new FileWriter(ymlFile)) {
                yaml.dump(ymlMap, writer);
                log.info("application.yml has been updated. ✅");
            }
        } else {
            log.info("application.yml is already up-to-date. 📄");
        }
    }

    /**
     * Helper method to safely get or create a nested map.
     *
     * @param parentMap The map to check for the key.
     * @param key       The key of the nested map.
     * @return The nested map, creating it if it doesn't exist.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> getOrCreateNestedMap(Map<String, Object> parentMap, String key) {
        return (Map<String, Object>) parentMap.computeIfAbsent(key, k -> new LinkedHashMap<>());
    }

    /**
     * Updates JDBC configuration in the YAML map.
     *
     * @param ymlMap          The overall YAML map.
     * @param generatorConfig The generator configuration with JDBC details.
     * @return True if modified, false otherwise.
     */
    private boolean updateJdbcConfiguration(Map<String, Object> ymlMap, GeneratorConfig generatorConfig) {
        boolean changed = false;
        Map<String, Object> springMap = getOrCreateNestedMap(ymlMap, SPRING_KEY);
        Map<String, Object> datasourceMap = getOrCreateNestedMap(springMap, DATASOURCE_KEY);

        if (!Objects.equals(generatorConfig.getBaseConfigs().getJdbcConfig().getUrl(), datasourceMap.get(URL_KEY))) {
            datasourceMap.put(URL_KEY, generatorConfig.getBaseConfigs().getJdbcConfig().getUrl());
            datasourceMap.put(USERNAME_KEY, generatorConfig.getBaseConfigs().getJdbcConfig().getUsername());
            datasourceMap.put(PASSWORD_KEY, generatorConfig.getBaseConfigs().getJdbcConfig().getPassword());
            datasourceMap.put(DRIVER_CLASS_NAME_KEY, generatorConfig.getBaseConfigs().getJdbcConfig().getDriver());
            log.info("Updated JDBC datasource configuration. 🔗");
            changed = true;
        }
        return changed;
    }

    /**
     * Updates Spring profiles configuration in the YAML map.
     *
     * @param ymlMap The overall YAML map.
     * @return True if modified, false otherwise.
     */
    private boolean updateSpringProfiles(Map<String, Object> ymlMap) {
        boolean changed = false;
        Map<String, Object> springMap = getOrCreateNestedMap(ymlMap, SPRING_KEY);
        Map<String, Object> profilesMap = getOrCreateNestedMap(springMap, PROFILES_KEY);

        if (!Objects.equals(DEV_PROFILE_VALUE, profilesMap.get(ACTIVE_KEY))) {
            profilesMap.put(ACTIVE_KEY, DEV_PROFILE_VALUE);
            log.info("Set Spring active profile to 'dev'. 🚀");
            changed = true;
        }
        return changed;
    }

    /**
     * Updates MyBatis Plus configuration in the YAML map.
     *
     * @param ymlMap The overall YAML map.
     * @return True if modified, false otherwise.
     */
    private boolean updateMybatisPlusConfiguration(Map<String, Object> ymlMap) {
        boolean changed = false;
        Map<String, Object> mybatisMap = getOrCreateNestedMap(ymlMap, MYBATIS_PLUS_KEY);

        // Mapper locations
        if (!Objects.equals(MAPPER_LOCATION_VALUE, mybatisMap.get(MAPPER_LOCATIONS_KEY))) {
            mybatisMap.put(MAPPER_LOCATIONS_KEY, MAPPER_LOCATION_VALUE);
            log.info("Updated MyBatis Plus mapper locations. 🗺️");
            changed = true;
        }

        // Configuration log-impl
        Map<String, Object> configurationMap = getOrCreateNestedMap(mybatisMap, CONFIGURATION_KEY);
        if (!Objects.equals(LOG_IMPL_VALUE, configurationMap.get(LOG_IMPL_KEY))) {
            configurationMap.put(LOG_IMPL_KEY, LOG_IMPL_VALUE);
            log.info("Set MyBatis Plus log implementation to StdOutImpl. ✍️");
            changed = true;
        }

        // ADDED: Global config for logic delete
        Map<String, Object> globalConfigMap = getOrCreateNestedMap(mybatisMap, GLOBAL_CONFIG_KEY);
        Map<String, Object> dbConfigMap = getOrCreateNestedMap(globalConfigMap, DB_CONFIG_KEY);

        if (!Objects.equals(LOGIC_DELETE_FIELD_VALUE, dbConfigMap.get(LOGIC_DELETE_FIELD_KEY))) {
            dbConfigMap.put(LOGIC_DELETE_FIELD_KEY, LOGIC_DELETE_FIELD_VALUE);
            log.info("Set MyBatis Plus logic delete field to 'deleted'. 🗑️");
            changed = true;
        }
        if (!Objects.equals(LOGIC_DELETE_VALUE, dbConfigMap.get(LOGIC_DELETE_VALUE_KEY))) {
            dbConfigMap.put(LOGIC_DELETE_VALUE_KEY, LOGIC_DELETE_VALUE);
            log.info("Set MyBatis Plus logic delete value to '1'. ✔️");
            changed = true;
        }
        if (!Objects.equals(LOGIC_NOT_DELETE_VALUE, dbConfigMap.get(LOGIC_NOT_DELETE_VALUE_KEY))) {
            dbConfigMap.put(LOGIC_NOT_DELETE_VALUE_KEY, LOGIC_NOT_DELETE_VALUE);
            log.info("Set MyBatis Plus logic not delete value to '0'. ✖️");
            changed = true;
        }
        return changed;
    }

    /**
     * Updates Swagger authentication configuration in the YAML map.
     *
     * @param ymlMap The overall YAML map.
     * @return True if modified, false otherwise.
     */
    private boolean updateSwaggerConfiguration(Map<String, Object> ymlMap) {
        boolean changed = false;
        Map<String, Object> swaggerMap = getOrCreateNestedMap(ymlMap, SWAGGER_KEY);
        Map<String, Object> authMap = getOrCreateNestedMap(swaggerMap, AUTH_KEY);

        if (!Objects.equals(ADMIN_VALUE, authMap.get(USERNAME_KEY))) {
            authMap.put(USERNAME_KEY, ADMIN_VALUE);
            log.info("Updated Swagger auth username to 'admin'. 👤");
            changed = true;
        }
        if (!Objects.equals(ADMIN_VALUE, authMap.get(PASSWORD_KEY))) {
            authMap.put(PASSWORD_KEY, ADMIN_VALUE);
            log.info("Updated Swagger auth password to 'admin'. 🔑");
            changed = true;
        }
        return changed;
    }
}
