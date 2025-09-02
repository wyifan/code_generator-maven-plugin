package ${templateConfig.packageInfo};

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
* Spring Security配置，用于保护Swagger UI接口
*/
@Configuration
public class WebSecurityConfig {

<#noparse>
    @Value("${project.swagger.username}")
    private String swaggerUsername;

    @Value("${project.swagger.password}")
    private String swaggerPassword;
</#noparse>

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                // 仅允许登录后访问Swagger UI相关路径
                .requestMatchers(
                    AntPathRequestMatcher.antMatcher("/swagger-ui.html"),
                    AntPathRequestMatcher.antMatcher("/swagger-ui/**"),
                    AntPathRequestMatcher.antMatcher("/v3/api-docs/**"),
                    AntPathRequestMatcher.antMatcher("/error")
                ).authenticated()
                // 其他所有路径都允许访问
                .anyRequest().permitAll()
            )
            .httpBasic(Customizer.withDefaults()); // 使用HTTP Basic认证
        return http.build();
    }

    /**
    * 配置内存用户，用于Swagger登录
    */
    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user = User.withDefaultPasswordEncoder()
                .username(swaggerUsername)
                .password(swaggerPassword)
                .roles("USER")
                .build();
        return new InMemoryUserDetailsManager(user);
    }
}