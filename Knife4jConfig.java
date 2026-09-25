package com.atguigo.springaiproject_1.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().info(new Info()
                .title("招聘智能助手 API")
                .description("Spring AI + DashScope + PgVector 示例项目")
                .version("1.0.0"));
    }
}