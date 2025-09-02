package com.yifan.code_generator_maven_plugin.model;

import lombok.Data;

@Data
public class TemplateConfig {
    private String templateDir;
    private String templateFile;
    private String fileNameFormat;
    private String fileType;
    /**
     *   TODO: 暂为支持,想法是，这个值是为了给其他项目生成用的，代码放置到src/main/resources目录下面的这个目录中，
     *   然后再使用这个目录以及package目录，但是这种还是没法做到拷贝就可以放到其他项目中用的情况，package信息还是需要更改，
     *   文件目录也是需要处理。
     *   因为package会带上当前项目的项目名称，暂时放弃。20250903
      */
    private String outputDir;
    private String packageInfo;
    private String packageSuffix;
    private String customParams;
    private boolean generateOnce;
    private boolean isPlugin = false;
}
