package com.ringgo.common.config.security

import com.ringgo.common.security.JwtAuthenticationFilter
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

private val log = KotlinLogging.logger {}

@Configuration
@EnableWebSecurity
class SecurityConfig(
    @Value("\${spring.profiles.active:local}") private val activeProfile: String,
    private val jwtAuthenticationFilter: JwtAuthenticationFilter
) {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        log.info { "=== Security Config 설정 ($activeProfile) ===" }

        http
            .csrf { it.disable() }
            .headers { headers ->
                headers.frameOptions { it.disable() } // H2 콘솔 허용
            }
            .sessionManagement { session ->
                // STATELESS로 통일 (JWT 토큰 인증 사용)
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers(
                        // === 시스템 엔드포인트 (항상 허용) ===
                        "/v1/health",
                        "/v1/version",
                        "/v1/cors-test",

                        // === 개발용 API (MVP 단계에서 필수) ===
                        "/v1/dev/**",  // DevAuthController

                        // === Actuator (모니터링용) ===
                        "/actuator/health",
                        "/actuator/info",
                        "/actuator/prometheus",  // 🔥 Prometheus 메트릭 수집
                        *if (activeProfile in listOf("local", "dev")) {
                            arrayOf(
                                "/actuator/metrics",
                                "/actuator/env" // 개발 환경에서만 추가 허용
                            )
                        } else {
                            emptyArray()
                        },

                        // === Swagger UI ===
                        "/swagger-ui/**",
                        "/v3/api-docs/**",

                        // === 정적 리소스 ===
                        "/",
                        "/favicon.ico",
                        "/error"
                    ).permitAll()

                    // === CORS 프리플라이트 요청 ===
                    .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()

                    // === MVP 단계: 모든 API 허용 (나중에 인증 구현) ===
                    .anyRequest().permitAll()  // TODO: JWT 인증 구현 후 authenticated()로 변경
            }
            .cors { cors ->
                cors.configurationSource(corsConfigurationSource())
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

        log.info { "✅ Security 설정 완료: JWT 인증 + STATELESS" }
        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()

        log.info { "=== CORS 설정 ($activeProfile) ===" }

        when (activeProfile) {
            "prod" -> {
                // 운영: 특정 도메인만 허용
                configuration.allowedOrigins = listOf(
                    "https://ring-go.kr",           // 소개 페이지
                    "https://www.ring-go.kr",       // 소개 페이지 (www)
                    "https://app.ring-go.kr",       // 🔥 메인 애플리케이션 (추가!)
                    "https://docs.ring-go.kr",      // Swagger 문서
                    "https://api.ring-go.kr"        // API 서버
                )
                configuration.allowCredentials = true
                log.info { "🔒 운영 CORS: 특정 도메인만 허용" }
            }
            "dev" -> {
                // 개발서버: 개발 도메인들 허용
                configuration.allowedOrigins = listOf(
                    "http://localhost:3000",        // Next.js 개발서버
                    "http://localhost:8080",        // 백엔드 로컬
                    "http://127.0.0.1:3000",
                    "http://127.0.0.1:8080",
                    "https://ring-go.kr",           // 소개 페이지
                    "https://www.ring-go.kr",       // 소개 페이지 (www)
                    "https://app.ring-go.kr",       // 🔥 메인 애플리케이션 (추가!)
                    "https://docs.ring-go.kr",      // Swagger 문서
                    "https://api.ring-go.kr"        // API 서버
                )
                configuration.allowCredentials = true
                log.info { "🚀 개발 CORS: 개발 도메인들 허용" }
            }
            else -> {
                // 로컬: 모든 Origin 허용 (개발 편의성)
                configuration.allowedOriginPatterns = listOf("*")
                configuration.allowCredentials = false  // 와일드카드 사용 시 필수
                log.info { "🏠 로컬 CORS: 모든 Origin 허용" }
            }
        }

        // 공통 설정
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
