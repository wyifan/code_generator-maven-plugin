package com.yifan.code_generator_maven_plugin.utils;

import com.yifan.code_generator_maven_plugin.model.DependencyInfo;
import com.yifan.code_generator_maven_plugin.model.PluginConfig;
import org.apache.maven.model.*;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

/**
 * package_name: com.yifan.codegen_maven_plugin.utils
 * author: wyifa
 * date: 2025/8/28 0:24
 * description: Maven依赖管理工具类，用于向合适的pom文件中添加依赖。
 */
public class DependencyMgmt {

    public static final String POM_XML = "pom.xml";

    /**
     * POM文件读取器接口，用于解耦和测试。
     */
    public interface PomReader {
        Model readModel(File pomFile) throws IOException, XmlPullParserException;

        void writeModel(Model model, File pomFile) throws IOException;
    }

    /**
     * 默认的POM读取器实现。
     */
    public static class DefaultPomReader implements PomReader {
        @Override
        public Model readModel(File pomFile) throws IOException, XmlPullParserException {
            MavenXpp3Reader reader = new MavenXpp3Reader();
            // FIXED: Use InputStreamReader with UTF-8 to prevent character encoding issues.
            try (InputStreamReader isr = new InputStreamReader(new FileInputStream(pomFile), StandardCharsets.UTF_8)) {
                return reader.read(isr);
            }
        }

        @Override
        public void writeModel(Model model, File pomFile) throws IOException {
            MavenXpp3Writer writer = new MavenXpp3Writer();
            // FIXED: Use OutputStreamWriter with UTF-8 to prevent character encoding
            // issues.
            try (OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(pomFile),
                    StandardCharsets.UTF_8)) {
                writer.write(osw, model);
            }
        }
    }

    private final PomReader pomReader;

    public DependencyMgmt() {
        this.pomReader = new DefaultPomReader();
    }

    /**
     * 用于测试的构造函数，允许注入模拟的PomReader。
     */
    public DependencyMgmt(PomReader pomReader) {
        this.pomReader = pomReader;
    }

    /**
     * Adds a list of dependencies to the appropriate POM file, using a MavenProject
     * object for context.
     * This is the primary entry point for a Maven plugin.
     *
     * @param mavenProject The current Maven project.
     * @param dependencies The list of dependencies to add.
     * @throws IOException            When an error occurs reading or writing the
     *                                POM file.
     * @throws XmlPullParserException When an error occurs parsing the POM file.
     */
    public void addDependenciesFromPlugin(MavenProject mavenProject, List<DependencyInfo> dependencies,
                                          List<PluginConfig> plugins)
            throws IOException, XmlPullParserException {
        File projectDir = mavenProject.getBasedir();
        File parentDir = mavenProject.getParent() != null ? mavenProject.getParent().getBasedir() : null;
        addDependenciesToAppropriatePom(projectDir, parentDir, dependencies, plugins);
    }

    // UPDATED: 此方法现在处理所有依赖，并在最后统一写入POM文件

    /**
     * 向合适的POM文件中添加多个依赖。
     *
     * @param projectDir   项目根目录
     * @param parentDir    父项目目录（如果当前项目是模块）
     * @param dependencies 要添加的依赖列表
     * @throws Exception 当处理POM文件时发生错误
     */
    public void addDependenciesToAppropriatePom(File projectDir, File parentDir, List<DependencyInfo> dependencies,
                                                List<PluginConfig> plugins)
            throws IOException, XmlPullParserException {
        File projectPomFile = new File(projectDir, POM_XML);
        Model projectModel = pomReader.readModel(projectPomFile);
        Model parentModel = null;
        if (parentDir != null) {
            File parentPomFile = new File(parentDir, POM_XML);
            parentModel = pomReader.readModel(parentPomFile);
        }

        for (DependencyInfo dependency : dependencies) {
            // 检查当前项目是否已包含此依赖
            if (hasDependency(projectModel.getDependencies(), dependency.getGroupId(), dependency.getArtifactId())) {
                continue; // 已存在，跳过
            }

            if (parentDir == null) {
                processDependencyWithoutParent(projectModel, dependency);
            } else {
                processDependencyWithParent(projectModel, parentModel, dependency);
            }
        }

        updateBuildPlugins(projectModel, plugins);

        // ADDED: 统一在循环外部写入文件，确保只写一次
        pomReader.writeModel(projectModel, projectPomFile);
    }

    // UPDATED: 原来的方法现在变为私有，并且不再负责文件写入

    /**
     * 向合适的POM文件中添加依赖。
     * * @param projectDir 项目根目录
     *
     * @param parentDir  父项目目录（如果当前项目是模块）
     * @param dependencyInfo 要添加的依赖
     * @throws Exception 当处理POM文件时发生错误
     */
    public void addDependencyToAppropriatePom(File projectDir, File parentDir, DependencyInfo dependencyInfo)
            throws IOException, XmlPullParserException {
        File projectPomFile = new File(projectDir, POM_XML);
        Model projectModel = pomReader.readModel(projectPomFile);

        // 检查当前项目是否已包含此依赖，如果已存在则直接返回
        if (hasDependency(projectModel.getDependencies(), dependencyInfo.getGroupId(), dependencyInfo.getArtifactId())) {
            return;
        }

        if (parentDir == null) {
            // 没有父POM，直接处理当前项目的依赖
            processDependencyWithoutParent(projectModel, projectPomFile, dependencyInfo);
        } else {
            // 有父POM，需要检查父POM
            File parentPomFile = new File(parentDir, POM_XML);
            Model parentModel = pomReader.readModel(parentPomFile);
            processDependencyWithParent(projectModel, projectPomFile, parentModel, dependencyInfo);
        }
    }

    // UPDATED: 移除了文件写入参数

    /**
     * 处理没有父POM的情况。
     */
    private void processDependencyWithoutParent(Model projectModel, DependencyInfo dependencyInfo) {
        boolean inDepMgmt = hasDependency(
                Optional.ofNullable(projectModel.getDependencyManagement())
                        .map(DependencyManagement::getDependencies)
                        .orElse(null),
                dependencyInfo.getGroupId(), dependencyInfo.getArtifactId());

        Dependency depToAdd = createDependency(dependencyInfo);

        if (!inDepMgmt) {
            addVersionToProperties(projectModel, depToAdd);
        } else {
            depToAdd.setVersion(null);
        }

        projectModel.addDependency(depToAdd);
    }

    // UPDATED: 移除了文件写入参数
    private void processDependencyWithParent(Model projectModel, Model parentModel, DependencyInfo dependencyInfo) {
        boolean inParentDepMgmt = hasDependency(
                Optional.ofNullable(parentModel.getDependencyManagement())
                        .map(DependencyManagement::getDependencies)
                        .orElse(null),
                dependencyInfo.getGroupId(), dependencyInfo.getArtifactId());

        boolean inParentDeps = hasDependency(parentModel.getDependencies(), dependencyInfo.getGroupId(),
                dependencyInfo.getArtifactId());

        if (inParentDeps) {
            return;
        }

        Dependency depToAdd = createDependency(dependencyInfo);

        if (inParentDepMgmt) {
            depToAdd.setVersion(null);
        } else {
            addVersionToProperties(projectModel, depToAdd);
        }

        projectModel.addDependency(depToAdd);
    }

    // UPDATED: 移除了文件写入参数
    private void processDependencyWithoutParent(Model projectModel, File projectPomFile, DependencyInfo dependencyInfo)
            throws IOException {
        boolean inDepMgmt = hasDependency(
                Optional.ofNullable(projectModel.getDependencyManagement())
                        .map(DependencyManagement::getDependencies)
                        .orElse(null),
                dependencyInfo.getGroupId(), dependencyInfo.getArtifactId());

        Dependency depToAdd = createDependency(dependencyInfo);

        if (!inDepMgmt) {
            // 如果dependencyManagement中没有，并且依赖有版本，则将版本添加到properties中
            addVersionToProperties(projectModel, depToAdd);
        } else {
            // 如果在dependencyManagement中，则移除版本信息
            depToAdd.setVersion(null);
        }

        projectModel.addDependency(depToAdd);
        pomReader.writeModel(projectModel, projectPomFile);
    }

    // UPDATED: 移除了文件写入参数
    private void processDependencyWithParent(Model projectModel, File projectPomFile, Model parentModel,
                                             DependencyInfo dependencyInfo) throws IOException {
        boolean inParentDepMgmt = hasDependency(
                Optional.ofNullable(parentModel.getDependencyManagement())
                        .map(DependencyManagement::getDependencies)
                        .orElse(null),
                dependencyInfo.getGroupId(), dependencyInfo.getArtifactId());

        // 检查父POM的dependencies中是否已存在
        boolean inParentDeps = hasDependency(parentModel.getDependencies(), dependencyInfo.getGroupId(),
                dependencyInfo.getArtifactId());

        if (inParentDeps) {
            // 父POM中已存在此依赖，不做任何处理
            return;
        }

        Dependency depToAdd = createDependency(dependencyInfo);

        if (inParentDepMgmt) {
            // 父POM的dependencyManagement中存在，添加到当前项目但不带版本
            depToAdd.setVersion(null);
        } else {
            // 父POM和当前项目中都不存在，将版本添加到properties中
            addVersionToProperties(projectModel, depToAdd);
        }

        projectModel.addDependency(depToAdd);
        pomReader.writeModel(projectModel, projectPomFile);
    }

    /**
     * 将版本信息添加到properties，并更新依赖的版本为属性引用。
     *
     * @param model      要更新的模型
     * @param dependency 要处理的依赖
     */
    private void addVersionToProperties(Model model, Dependency dependency) {
        if (dependency.getVersion() == null) {
            return;
        }

        String propertyName = generatePropertyName(dependency.getGroupId(), dependency.getArtifactId());
        Properties properties = Optional.ofNullable(model.getProperties())
                .orElseGet(() -> {
                    model.setProperties(new Properties());
                    return model.getProperties();
                });

        if (!properties.containsKey(propertyName)) {
            properties.setProperty(propertyName, dependency.getVersion());
        }

        // 更新依赖版本为属性引用
        dependency.setVersion("${" + propertyName + "}");
    }

    /**
     * Updates or adds build plugins to the project's POM file.
     * The logic checks for existing plugins and either adds them or updates their
     * configuration.
     *
     * @param model         The current Maven project.
     * @param pluginConfigs The list of plugins to be added or updated.
     * @throws IOException            If an I/O error occurs while reading or
     *                                writing the POM file.
     * @throws XmlPullParserException If the POM file is malformed.
     */
    public void updateBuildPlugins(Model model, List<PluginConfig> pluginConfigs)
            throws IOException, XmlPullParserException {

        // 1. Check for the <build> section and create if it doesn't exist.
        Build build = Optional.ofNullable(model.getBuild()).orElseGet(Build::new);
        model.setBuild(build);

        List<Plugin> existingPlugins = build.getPlugins();

        // 2. Iterate through the list of plugins to add/update
        for (PluginConfig pluginToAdd : pluginConfigs) {

            // Find an existing plugin with the same groupId and artifactId
            Optional<Plugin> existingPlugin = existingPlugins.stream()
                    .filter(p -> p.getArtifactId().equals(pluginToAdd.getArtifactId()))
                    .findFirst();

            if (existingPlugin.isPresent()) {
                existingPlugin.get().setVersion(pluginToAdd.getVersion());
                Xpp3Dom config = new Xpp3Dom("configuration");
                Xpp3Dom source = new Xpp3Dom("source");
                source.setValue("${java.version}");
                Xpp3Dom target = new Xpp3Dom("target");
                target.setValue("${java.version}");
                config.addChild(source);
                config.addChild(target);
                existingPlugin.get().setConfiguration(config);

                // log.info("Plugin " + pluginToAdd.getArtifactId() + " already exists. Updating
                // configuration if needed.");
            } else {
                 Plugin compiler = new Plugin();
                 compiler.setGroupId(pluginToAdd.getGroupId());
                 compiler.setArtifactId(pluginToAdd.getArtifactId());
                 pluginToAdd.setVersion(pluginToAdd.getVersion());
                 Xpp3Dom config = new Xpp3Dom("configuration");
                 Xpp3Dom source = new Xpp3Dom("source");
                 source.setValue("${java.version}");
                 Xpp3Dom target = new Xpp3Dom("target");
                 target.setValue("${java.version}");
                 config.addChild(source);
                 config.addChild(target);
                 compiler.setConfiguration(config);

                 // log.info("Adding new plugin: " + pluginToAdd.getArtifactId());
                 build.addPlugin(compiler);


            }
        }

    }

    /**
     * 生成properties键名。
     */
    private String generatePropertyName(String groupId, String artifactId) {
        return artifactId.replace('.', '-') + ".version";
    }

    /**
     * 检查依赖列表中是否包含指定依赖。
     *
     * @param dependencies 依赖列表
     * @param groupId      依赖的groupId
     * @param artifactId   依赖的artifactId
     * @return 如果存在则返回true
     */
    private boolean hasDependency(List<Dependency> dependencies, String groupId, String artifactId) {
        if (dependencies == null) {
            return false;
        }
        return dependencies.stream()
                .anyMatch(dep -> groupId.equals(dep.getGroupId()) && artifactId.equals(dep.getArtifactId()));
    }

    private Dependency createDependency(DependencyInfo dependencyInfo) {
        Dependency mavenDependency = new Dependency();
        mavenDependency.setGroupId(dependencyInfo.getGroupId());
        mavenDependency.setArtifactId(dependencyInfo.getArtifactId());
        mavenDependency.setVersion(dependencyInfo.getVersion());
        mavenDependency.setScope(dependencyInfo.getScope());

        return mavenDependency;
    }

    /**
     * 复制依赖对象。
     */
    private Dependency cloneDependency(Dependency original) {
        return original.clone();
    }
}
