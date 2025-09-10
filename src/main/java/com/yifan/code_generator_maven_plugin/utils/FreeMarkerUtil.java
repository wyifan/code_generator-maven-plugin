package com.yifan.code_generator_maven_plugin.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yifan.code_generator_maven_plugin.common.CommonFunc;
import com.yifan.code_generator_maven_plugin.constant.Constants;
import com.yifan.code_generator_maven_plugin.model.*;
import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;


public class FreeMarkerUtil {

    private Configuration cfg;
    private static final String DEFAULT_ENCODING = "UTF-8";

    private Log log;
    private MavenProject project;

    public FreeMarkerUtil() throws IOException {
        cfg = new Configuration(Configuration.VERSION_2_3_32);
        cfg.setDefaultEncoding(DEFAULT_ENCODING);
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setLogTemplateExceptions(false);
        cfg.setWrapUncheckedExceptions(true);
    }

    /**
     * 根据配置文件处理模板生成
     *
     * @param config       POJO 形式的完整配置
     * @param mavenProject Maven项目对象
     */
    public void processTemplates(GeneratorConfig config, MavenProject mavenProject, Log log) {
        this.log = log;
        this.project = mavenProject;
        ModelSetting modelSetting = config.getModelSettings();
        BaseConfig baseConfig = config.getBaseConfigs();

        String templateAppendMode = modelSetting.getTemplateAppendMode();

        String basePackage = baseConfig.getBasePackagePrefix();
        String templateDir; // 不能直接取modelSetting中的templateDir，用户如果配置了modelSetting，那么这个值会被写成用户配置的，从而影响到插件内部模板
        for (TableConfig tc : modelSetting.getTables()) {
            for (TemplateConfig templateConfig : modelSetting.getTemplates()) {

                String packageSuffix = templateConfig.getPackageSuffix();
                String projectPackage = basePackage + "." + mavenProject.getArtifactId().toLowerCase().replace("-", "_");
                if (packageSuffix != null && !packageSuffix.isEmpty()) {
                    // 填充templateConfig中packageInfo信息
                    templateConfig.setPackageInfo(projectPackage + "." + packageSuffix);
                } else {
                    // 填充templateConfig中packageInfo信息
                    templateConfig.setPackageInfo(projectPackage);
                }
                templateDir = templateConfig.getTemplateDir();
                printInfoLog("Template:" + templateConfig.getTemplateFile() + " dir:" + templateDir);

                // 调用内部生成方法
                this.generate(tc,
                        templateConfig, templateDir, templateAppendMode, projectPackage, config, mavenProject);
            }
        }
    }

    /**
     * 根据配置生成代码文件（私有方法）
     *
     * @param templateConfig     单个模板的配置POJO
     * @param templateDir        模板存放目录
     * @param templateAppendMode 模板生成模式
     * @param projectPackage     项目包名
     * @param fullConfig         完整的GeneratorConfig对象
     * @param mavenProject       Maven项目对象
     */
    private void generate(
            TableConfig tableConfig,
            TemplateConfig templateConfig,
            String templateDir,
            String templateAppendMode,
            String projectPackage,
            GeneratorConfig fullConfig,
            MavenProject mavenProject) {
        File projectBaseDir = mavenProject.getBasedir();

        try {
            // 1. 设置模板加载器
            TemplateLoader templateLoader;
            boolean isPlugin = templateConfig.isPlugin();
            printInfoLog("isPlugin: " + isPlugin);
            printInfoLog("Base Dir: " + projectBaseDir);
            printInfoLog("absolute dir: " + projectBaseDir.getAbsolutePath());
            printInfoLog("templateDir: " + templateDir);
            if (isPlugin) {
                printInfoLog("using class load to read template: " + templateDir);
                templateLoader = new ClassTemplateLoader(this.getClass(), "/" + templateDir);
            } else {
                templateLoader = new FileTemplateLoader(new File(projectBaseDir, templateDir));
            }
            cfg.setTemplateLoader(templateLoader);

            // 2. 构建数据模型，FreeMarker可以直接处理POJO
            Map<String, Object> dataModel = buildDataModel(tableConfig, templateConfig, projectPackage, fullConfig);

            // 3. 获取FreeMarker模板实例
            Template template = cfg.getTemplate(templateConfig.getTemplateFile());

            // 4. 渲染文件名
            Template fileNameTpl = new Template("fileName", new StringReader(templateConfig.getFileNameFormat()), cfg);
            StringWriter fileNameWriter = new StringWriter();
            fileNameTpl.process(dataModel, fileNameWriter);
            String generatedFileName = fileNameWriter.toString();

            String outputSourceDir = computeOutputPath(templateConfig);

            // 拼接 outputDir 和 outputSourceDir
            File targetDir = new File(projectBaseDir, outputSourceDir);
            if (!targetDir.exists()) {
                targetDir.mkdirs();
            }

            File outputFile = new File(targetDir, generatedFileName);
            // 6. 处理 overwrite 和 generateOnce 逻辑,认为overwrite已经不需要处理了 20250903
            if (outputFile.exists()) { // 文件已经存在便不生成
                printInfoLog("Skipped file File exist : " + outputFile.getAbsolutePath());
                return;
            }
            if (templateConfig.isGenerateOnce() && outputFile.exists()) {
                printInfoLog("Skipped file (generateOnce mode): " + outputFile.getAbsolutePath());
                return;
            }

            // 7. 渲染模板并写入文件
            try (Writer fileWriter = new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8)) {
                template.process(dataModel, fileWriter);
                System.out.println("Generated file: " + outputFile.getAbsolutePath());
            }

        } catch (IOException | TemplateException e) {
            e.printStackTrace();
        }
    }

    private String computeOutputPath(TemplateConfig templateConfig) {
        // 5. 确定输出路径
        String outputSourceDir;
        if (Constants.TemplateConstants.FILE_TYPE_JAVA.equalsIgnoreCase(templateConfig.getFileType())) {
            outputSourceDir = Constants.FileConstant.JAVA_RESOURCE_PATH;
            if (templateConfig.getPackageInfo() != null && !templateConfig.getPackageInfo().isEmpty()) {
                outputSourceDir += File.separator + templateConfig.getPackageInfo().replace('.', File.separatorChar);
            }
        } else if (Constants.TemplateConstants.FILE_TYPE_RESOURCE.equalsIgnoreCase(templateConfig.getFileType())) {
            outputSourceDir = Constants.FileConstant.MAPPER_PATH;
        } else {
            outputSourceDir = "";
        }

        printInfoLog("Output Source Dir: " + outputSourceDir);
        return outputSourceDir;
    }

    /**
     * 构建FreeMarker数据模型
     *
     * @param templateConfig 单个模板配置
     * @param projectPackage 项目包
     * @param fullConfig     完整的配置POJO
     * @return 完整的数据模型Map
     */
    private Map<String, Object> buildDataModel(TableConfig tc,
                                               TemplateConfig templateConfig, String projectPackage, GeneratorConfig fullConfig)
            throws IOException {
        Map<String, Object> dataModel = new HashMap<>();

        // 1. 将完整的POJO放入数据模型
//        dataModel.put("config", fullConfig);
        dataModel.put("templateConfig", templateConfig);
        dataModel.put("entityName", tc.getEntityName());
        dataModel.put("tableName", tc.getTableName());
        dataModel.put("tableConfig", tc);
        dataModel.put("useBaseEntity", tc.isUseBaseEntity());
        dataModel.put("moduleName", CommonFunc.convertToPascalCase(project.getArtifactId()));

        // 2. 添加常用的公共参数，方便模板直接访问
        dataModel.put("packageName", templateConfig.getPackageInfo());

        // 添加基础的一些数据，便于处理import信息
        addCommonPackageInfo(dataModel, projectPackage);

        // 3. 解析customParams并覆盖现有数据
        String customParamsJson = templateConfig.getCustomParams();
        if (customParamsJson != null && !customParamsJson.isEmpty()) {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> customParams = mapper.readValue(customParamsJson, new TypeReference<Map<String, Object>>() {
            });
            dataModel.putAll(customParams);
        }

        return dataModel;
    }

    /**
     * 创建模板中可以使用的基础包名，包含一般项目中所有的基础包名，便于模板规范化
     *
     * @param dataModel
     * @param projectPackage
     */
    private void addCommonPackageInfo(Map<String, Object> dataModel, String projectPackage) {
        dataModel.put("projectPackage", projectPackage);
        dataModel.put("basePackage", projectPackage + "." + Constants.PackageSuffix.BASE);
        dataModel.put("commonPackage", projectPackage + "." + Constants.PackageSuffix.COMMON);
        dataModel.put("utilsPackage", projectPackage + "." + Constants.PackageSuffix.UTILS);
        dataModel.put("voPackage", projectPackage + "." + Constants.PackageSuffix.VO);
        dataModel.put("dtoPackage", projectPackage + "." + Constants.PackageSuffix.DTO);
        dataModel.put("servicePackage", projectPackage + "." + Constants.PackageSuffix.SERVICE);
        dataModel.put("serviceImplPackage", projectPackage + "." + Constants.PackageSuffix.SERVICE_IMPL);
        dataModel.put("controllerPackage", projectPackage + "." + Constants.PackageSuffix.CONTROLLER);
        dataModel.put("entityPackage", projectPackage + "." + Constants.PackageSuffix.ENTITY);
        dataModel.put("mapperPackage", projectPackage + "." + Constants.PackageSuffix.MAPPER);
        dataModel.put("configPackage", projectPackage + "." + Constants.PackageSuffix.CONFIG);
        dataModel.put("handlerPackage", projectPackage + "." + Constants.PackageSuffix.HANDLER);
        dataModel.put("interceptorPackage", projectPackage + "." + Constants.PackageSuffix.INTERCEPTOR);
        dataModel.put("exceptionPackage", projectPackage + "." + Constants.PackageSuffix.EXCEPTION);

    }

    private void printInfoLog(String logInfo) {
        if (log.isInfoEnabled()) {
            log.info(logInfo);
        }
    }
}
