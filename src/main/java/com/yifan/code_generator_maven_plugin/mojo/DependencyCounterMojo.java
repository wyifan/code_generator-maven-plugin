package com.yifan.code_generator_maven_plugin.mojo;


import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.util.List;

@Mojo(name = "dependency-counter", defaultPhase = LifecyclePhase.COMPILE, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class DependencyCounterMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    MavenProject project;

    @Parameter(property = "scope")
    String scope;

    public void execute() throws MojoExecutionException, MojoFailureException {

        String projectPath = System.getProperty("user.dir");
        getLog().info("当前项目路径: " + projectPath);

        getLog().info("Initializing DependencyCounterMojo");
        List<Dependency> dependencies = project.getDependencies();

        long numDependencies = dependencies.stream()
                .filter(dep -> {
                    getLog().info("dependency scope: " + dep.getScope());
                    return scope == null || scope.isEmpty() || scope.equalsIgnoreCase(dep.getScope());
                })
                .count();
        // getLog() provides access to the logging system
        getLog().info("parameter scope: " + scope);
        getLog().info("Number of dependencies: " + numDependencies);
    }
}

