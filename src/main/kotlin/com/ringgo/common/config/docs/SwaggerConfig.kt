package com.ringgo.common.config.docs

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig(
    @Value("\${swagger.server.url}") private val serverUrl: String,
    @Value("\${swagger.server.description}") private val serverDescription: String
) {
    @Bean
    fun openAPI(): OpenAPI = OpenAPI()
        .info(
            Info()
                .title("Ring-Go API")
                .description("링고 서비스 API 문서")
                .version("1.0.0")
        )
        .servers(
            listOf(
                Server().url(serverUrl).description(serverDescription)
            )
        )
        .components(
            Components()
                .addSecuritySchemes("bearer-key",
                    SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                )
        )
}