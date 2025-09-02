package com.yifan.code_generator_maven_plugin.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.yifan.code_generator_maven_plugin.common.CommonFunc;
import com.yifan.code_generator_maven_plugin.constant.Constants;
import com.yifan.code_generator_maven_plugin.model.*;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public final class ConfigLoaderUtil {
    private static volatile ConfigLoaderUtil instance = null;
    private static final Gson GSON_PRINTER = new GsonBuilder().setPrettyPrinting().create();

    private final MavenProject mavenProject;
    private final Log log;

    private ConfigLoaderUtil(MavenProject mavenProject, Log log) {
        this.mavenProject = mavenProject;
        this.log = log;
    }

    /***
     * 单例方法
     * @param mavenProject
     * @param log
     * @return
     */
    public static ConfigLoaderUtil getInstance(MavenProject mavenProject, Log log) {
        if (instance == null) {
            synchronized (ConfigLoaderUtil.class) {
                if (instance == null) {
                    instance = new ConfigLoaderUtil(mavenProject, log);
                }
            }
        }
        return instance;
    }

    /**
     * 对外接口，返回调用插件后合并得到的最终配置信息
     *
     * @return
     */
    public GeneratorConfig getGeneratorConfig() throws FileNotFoundException, IOException {
        // 读取插件内部配置信息
        GeneratorConfig pluginConfig = loadConfigFromClasspath(Constants.FileConstant.CONFIG_PATH);
        printInfoLog("读取插件中配置信息：", pluginConfig);
        GeneratorConfig userConfig = loadUserConfigFromPath();
        printInfoLog("读取用户配置信息：", userConfig);

        GeneratorConfig mergedConfig = mergeConfigs(pluginConfig, userConfig);
        printInfoLog("合并后处理配置信息：", mergedConfig);

        return mergedConfig;
    }

    /**
     * Loads GeneratorConfig from the plugin's classpath resource.
     *
     * @param resourcePath The path to the YAML resource in the classpath.
     * @return A populated GeneratorConfig object.
     * @throws IOException If the resource is not found or an I/O error occurs.
     */
    private GeneratorConfig loadConfigFromClasspath(String resourcePath) throws IOException {
        try (InputStream inputStream = ConfigLoaderUtil.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                log.error("插件默认路径缺失配置：" + resourcePath);
                throw new FileNotFoundException("Classpath resource not found: " + resourcePath);
            }
            return loadConfig(inputStream);
        }
    }

    private void printInfoLog(String logInfo) {
        if (log.isInfoEnabled()) {
            log.info(logInfo);
        }
    }

    private <T> void printInfoLog(String logInfo, T t) {
        if (log.isInfoEnabled()) {
            log.info(logInfo + GSON_PRINTER.toJson(t));
        }
    }

    private GeneratorConfig loadUserConfigFromPath() throws IOException {
        Path userConfigPath = Paths.get(mavenProject.getBasedir().getAbsolutePath(),
                Constants.FileConstant.RESOURCE_PATH,
                Constants.FileConstant.CONFIG_PATH);
        log.info("Loading user config from " + userConfigPath);
        try (InputStream inputStream = new FileInputStream(userConfigPath.toFile())) {
            return loadConfig(inputStream);
        }
    }

    /**
     * Loads GeneratorConfig from an InputStream.
     *
     * @param inputStream The input stream containing YAML data.
     * @return A populated GeneratorConfig object.
     * @throws IOException If an I/O error occurs while reading the stream.
     */
    private GeneratorConfig loadConfig(InputStream inputStream) throws IOException {
        Yaml yaml = new Yaml();
        // 确保使用 InputStreamReader 并指定 UTF-8 编码来处理字符流
        try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            return yaml.loadAs(reader, GeneratorConfig.class);
        }
    }

    /**
     * 处理合并配置信息:
     * 1. baseConfigs: 直接使用userConfig中的数据，但typeMapping需要考虑是合并处理，userConfig中的同名会覆盖前者
     * 2. modelSettings与pomSetting都是根据appendMode进行处理，overwrite模式下，直接忽略插件中配置，但properties节除外，必须包含插件总配置的值
     * 3. swagger配置以userConfig为准，用户没有配置时，使用插件中配置
     *
     * @param pluginConfig
     * @param userConfig
     * @return
     */
    private GeneratorConfig mergeConfigs(GeneratorConfig pluginConfig, GeneratorConfig userConfig) throws IllegalArgumentException {
        GeneratorConfig mergedConfig = new GeneratorConfig();

        // baseConfigs
        mergedConfig.setBaseConfigs(mergeBaseConfig(pluginConfig.getBaseConfigs(), userConfig.getBaseConfigs()));

        // modelSettings
        mergedConfig.setModelSettings(mergeModelSetting(pluginConfig.getModelSettings(), userConfig.getModelSettings()));

        // pomSetting
        mergedConfig.setPomSettings(mergePomSetting(pluginConfig.getPomSettings(), userConfig.getPomSettings()));

        // swagger
        if (userConfig.getSwagger() != null) {
            mergedConfig.setSwagger(userConfig.getSwagger());
        } else {
            mergedConfig.setSwagger(pluginConfig.getSwagger());
        }

        return mergedConfig;
    }

    /**
     * 处理baseconfig合并： 用户的配置覆盖插件内部配置，但typeMapping需要将二者合并，同时如果存在重复，以用户的为准
     *
     * @param pluginBaseConfig
     * @param userBaseConfig
     * @return
     * @throws IllegalArgumentException
     */
    private BaseConfig mergeBaseConfig(BaseConfig pluginBaseConfig, BaseConfig userBaseConfig) throws IllegalArgumentException {
        if (userBaseConfig == null) {
            throw new IllegalArgumentException("用户配置文件缺失baseConfigs信息！");
        }
        if (userBaseConfig.getJdbcConfig() == null) {
            throw new IllegalArgumentException("用户配置文件缺失jdbcConfig信息！");
        }

        BaseConfig mergedBaseConfig = new BaseConfig();
        mergedBaseConfig.setJdbcConfig(userBaseConfig.getJdbcConfig());
        mergedBaseConfig.setBasePackagePrefix(userBaseConfig.getBasePackagePrefix());

        // 处理typeMapping合并处理
        Map<String, String> mergedTypeMapping = CommonFunc.mergeMaps(pluginBaseConfig.getTypeMapping(),
                userBaseConfig.getTypeMapping(),
                Constants.MergeStrategy.KEEP_SECOND);
        mergedBaseConfig.setTypeMapping(mergedTypeMapping);

        return mergedBaseConfig;
    }

    /**
     * 合并模板的信息
     *
     * @param pluginSetting
     * @param userSetting
     * @return
     */
    private ModelSetting mergeModelSetting(ModelSetting pluginSetting, ModelSetting userSetting) {
        pluginSetting.setPlugin(true);
        pluginSetting.getTemplates()
                .forEach(t -> {
                    t.setPlugin(true);
                    t.setTemplateDir(pluginSetting.getTemplateDir());
                });

        if (userSetting == null) {
            return pluginSetting;
        }
        // 补全信息，避免出问题,信息都放置向下一层，模板生成时，可以直接取，避免需要再向上一层的问题
        userSetting.getTemplates()
                .forEach(t -> {
                    String templateDir = userSetting.getTemplateDir();
                    t.setTemplateDir(templateDir == null || templateDir.isBlank()
                            ? Constants.FileConstant.RESOURCE_PATH + Constants.FileConstant.TEMPLATE_PATH
                            : userSetting.getTemplateDir());
                });

        ModelSetting mergedSetting = new ModelSetting();

        List<TableConfig> pluginTables = pluginSetting.getTables();
        List<TableConfig> userTables = userSetting.getTables();
        // 覆盖模式
        if (Constants.ConfigModeConstant.OVERWRITE_MODE.equalsIgnoreCase(userSetting.getTableAppendMode())) {
            mergedSetting.setTables(userTables);
        } else {// 合并模式，以用户配置为准
            mergedSetting.setTables(CommonFunc.mergeLists(pluginTables, userTables, TableConfig::getEntityName));
        }

        // 处理模板信息: templateDir插件内部模板的地址不需要用到这个值，依然从默认地址中取，用户的才会用到这个地址
        List<TemplateConfig> pluginTemplates = pluginSetting.getTemplates();
        pluginTemplates.forEach(t -> t.setPlugin(true)); // 必须要处理，避免合并模式下，找不到正确模板问题。

        List<TemplateConfig> userTemplates = userSetting.getTemplates();
        if (Constants.ConfigModeConstant.OVERWRITE_MODE.equalsIgnoreCase(userSetting.getTemplateAppendMode())) {
            mergedSetting.setTemplates(userTemplates);
        } else { //合并模式
            mergedSetting.setTemplates(CommonFunc.mergeLists(pluginTemplates, userTemplates, TemplateConfig::getTemplateFile));
        }
        mergedSetting.setTemplateDir(userSetting.getTemplateDir());

        return mergedSetting;

    }

    /**
     * 合并处理依赖、插件信息
     *
     * @param pluginSetting
     * @param userSetting
     * @return
     */
    private PomSetting mergePomSetting(PomSetting pluginSetting, PomSetting userSetting) {
        if (userSetting == null) {
            return pluginSetting;
        }
        PomSetting mergePomSetting = new PomSetting();
        // 处理properties
        mergePomSetting.setProperties(CommonFunc.mergeMaps(pluginSetting.getProperties()
                , userSetting.getProperties()
                , Constants.MergeStrategy.KEEP_SECOND));

        // 处理依赖,按groupId和artifactId
        if (Constants.ConfigModeConstant.OVERWRITE_MODE.equalsIgnoreCase(userSetting.getDependencyMode())) {
            mergePomSetting.setDependencies(userSetting.getDependencies());
        } else { // 合并模式
            mergePomSetting.setDependencies(CommonFunc.mergeLists(pluginSetting.getDependencies(),
                    userSetting.getDependencies(),
                    dependencyInfo -> dependencyInfo.getGroupId() + ":" + dependencyInfo.getArtifactId()));
        }
        // 处理plugin
        if (Constants.ConfigModeConstant.OVERWRITE_MODE.equalsIgnoreCase(userSetting.getPluginMode())) {
            mergePomSetting.setBuildPlugins(userSetting.getBuildPlugins());
        } else {
            mergePomSetting.setBuildPlugins(CommonFunc.mergeLists(pluginSetting.getBuildPlugins(),
                    userSetting.getBuildPlugins(),
                    pluginConfig -> pluginConfig.getGroupId() + ":" + pluginConfig.getArtifactId()));
        }

        return mergePomSetting;
    }
}
