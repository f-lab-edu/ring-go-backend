package com.ringgo.common.config.security

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

private val log = KotlinLogging.logger {}

@Configuration
@EnableWebSecurity
class SecurityConfig(
    @Value("\${spring.profiles.active:local}") private val activeProfile: String
) {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        log.info { "=== Security Config 설정 ($activeProfile) ===" }

        http
            .csrf { it.disable() }
            .headers { headers ->
                headers.frameOptions { it.disable() }
            }
            .authorizeHttpRequests { auth ->
                auth.anyRequest().permitAll()
            }
            .cors { cors ->
                cors.configurationSource(corsConfigurationSource())
            }

        log.info { "✅ Security 설정 완료: 모든 요청 허용 (임시)" }
        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()

        log.info { "=== CORS 설정 ($activeProfile) ===" }

        when (activeProfile) {
            "prod" -> {
                configuration.allowedOrigins = listOf(
                    "https://ring-go.kr",
                    "https://www.ring-go.kr",
                    "https://app.ring-go.kr",
                    "https://docs.ring-go.kr",
                    "https://api.ring-go.kr"
                )
                configuration.allowCredentials = true
                log.info { "🔒 운영 CORS: 특정 도메인만 허용" }
            }
            "dev" -> {
                configuration.allowedOrigins = listOf(
                    "http://localhost:3000",
                    "http://localhost:8080",
                    "https://dev.ring-go.kr",      // 프론트엔드
                    "https://api-dev.ring-go.kr",  // API 서버
                    "https://ring-go.kr",
                    "https://www.ring-go.kr"
                )
                configuration.allowCredentials = true
                log.info { "🚀 개발 CORS: 개발 도메인들 허용" }
            }
            else -> {
                configuration.allowedOriginPatterns = listOf("*")
                configuration.allowCredentials = false
                log.info { "🏠 로컬 CORS: 모든 Origin 허용" }
            }
        }

        configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
        configuration.allowedHeaders = listOf("*")
        configuration.exposedHeaders = listOf(
            "Authorization",
            "Content-Type",
            "X-Total-Count"
        )
        configuration.maxAge = 3600

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)

        log.info { "✅ CORS 설정 완료" }
        return source
    }
}
