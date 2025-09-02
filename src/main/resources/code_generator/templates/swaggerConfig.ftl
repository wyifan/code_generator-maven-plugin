package ${templateConfig.packageInfo};

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
* Swagger/OpenAPI配置类
* 只有当 project.api-docs.type 属性为 'swagger' 或 'all' 时才启用
*/
@Configuration
@ConditionalOnProperty(name = "project.api-docs.type", havingValue = "swagger", matchIfMissing = false)
@SecurityScheme(name = "basicAuth", type = SecuritySchemeType.HTTP, scheme = "basic")
public class SwaggerConfig {

<#noparse>
    @Value("${spring.application.name}")
    private String applicationName;
</#noparse>
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
        .info(new Info().title(applicationName + " API Documentation").version("1.0"))
        .addServersItem(new Server().url("/").description("Default Server URL"));
    }
}
