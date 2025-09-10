package com.yifan.code_generator_maven_plugin.mojo;

import com.yifan.code_generator_maven_plugin.model.GeneratorConfig;
import com.yifan.code_generator_maven_plugin.utils.*;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

@Mojo(name = "generate-code")
public class CrudGeneratorMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    /**
     * Whether to synchronize schema changes to the database. If false, only SQL
     * scripts will be
     * generated. Can be set via command line: -DsyncDb=true
     */
    @Parameter(property = "syncDb", defaultValue = "false")
    private boolean syncDb;

    @Override
    public void execute() {
        printInfoLog("Generating code...");
        try {
            // 1 读取配置
            GeneratorConfig config = ConfigLoaderUtil.getInstance(project, getLog()).getGeneratorConfig();

            // 2. 处理pom文件
            printInfoLog("Begin to handle pom file...");
            DependencyMgmt dependencyMgmt = new DependencyMgmt();
            dependencyMgmt.addDependenciesFromPlugin(project, config.getPomSettings().getDependencies(),
                    config.getPomSettings().getBuildPlugins(), config.getPomSettings().getResourceConfig());
            printInfoLog("End handle pom file.");

            // 3. 处理application.yml生成
            printInfoLog("Begin to generate application.xml...");
            ApplicationYmlUpdater applicationYmlUpdater = new ApplicationYmlUpdater(project, getLog());
            applicationYmlUpdater.updateApplicationYml(config);
            printInfoLog("End generate application.xml.");

            // 4. 处理启动类生成
            // 交由模板生成

            // 5. 处理sql生成
            printInfoLog("Begin to generate sql schema...");
            SchemaSynchronizer schemaSynchronizer = new SchemaSynchronizer(project, config, getLog());
            schemaSynchronizer.syncSchema(config.getModelSettings().getTables(), syncDb);
            printInfoLog("End generate sql schema.");

            // 6. 处理模板生成
            printInfoLog("Begin to generate file from templates...");
            FreeMarkerUtil codeGenerator = new FreeMarkerUtil();
            codeGenerator.processTemplates(config, project, getLog());
            printInfoLog("End generate file from templates.");

            printInfoLog("Generate code COMPLETED!");
        } catch (Exception e) {
            getLog().error(e);
        }
    }

    /**
     * 控制日志级别，如果使用比info更高日志级别时，不执行方法。
     *
     * @param logInfo
     */
    private void printInfoLog(String logInfo) {
        if (getLog().isInfoEnabled()) {
            getLog().info(logInfo);
        }
    }
}
