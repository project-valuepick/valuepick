package com.example.demo.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    private static final String JWT_SCHEME_NAME = "JWT";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("ValuePick API")
                .description("가치투자 지표 기반 종목 스크리닝 서비스 API 문서")
                .version("v1"))
            .addSecurityItem(new SecurityRequirement().addList(JWT_SCHEME_NAME))
            .components(new Components()
                .addSecuritySchemes(JWT_SCHEME_NAME, new SecurityScheme()
                    .name(JWT_SCHEME_NAME)
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")));
    }
}
