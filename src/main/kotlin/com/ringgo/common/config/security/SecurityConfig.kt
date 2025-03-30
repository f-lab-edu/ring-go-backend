package com.ringgo.common.config.security

import com.ringgo.common.config.security.jwt.JwtAuthenticationFilter
import com.ringgo.common.config.security.jwt.JwtTokenProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtTokenProvider: JwtTokenProvider,
    private val devMockUserFilter: DevMockUserFilter,
    private val customAuthenticationEntryPoint: CustomAuthenticationEntryPoint,
    private val customAccessDeniedHandler: CustomAccessDeniedHandler,
    private val pendingUserCheckFilter: PendingUserCheckFilter,
    private val userIdMatchFilter: UserIdMatchFilter,
) {
    // H2 콘솔용 별도 SecurityFilterChain
    @Bean
    @Order(1)
    fun h2ConsoleSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .securityMatcher(AntPathRequestMatcher("/h2-console/**"))
            .authorizeHttpRequests { auth ->
                auth.requestMatchers(AntPathRequestMatcher("/h2-console/**")).permitAll()
            }
            .csrf { it.disable() }
            .headers { headers ->
                headers.frameOptions { it.disable() }
            }
            .httpBasic { it.disable() }
            .formLogin { it.disable() }
            .logout { it.disable() }
            .build()
    }

    // 기존 API용 SecurityFilterChain
    @Bean
    @Order(2)
    fun apiSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        // JWT 인증 필터 생성
        val jwtFilter = JwtAuthenticationFilter(jwtTokenProvider)

        return http
            .csrf { it.disable() }
            // 일반 API에 대한 보안 헤더 설정
            .headers { headers ->
                headers
                    .frameOptions { it.deny() }
                    .xssProtection { xss ->
                        xss.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK)
                    }
                    .contentSecurityPolicy { csp ->
                        csp.policyDirectives("default-src 'self'; style-src 'self' 'unsafe-inline'; img-src 'self' data:;")
                    }
            }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    // 문서 및 개발 도구
                    .requestMatchers(
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/favicon.ico",
                        "/static/**",
                    ).permitAll()

                    // 인증 관련 엔드포인트
                    .requestMatchers(
                        "/auth/authorize/**",
                        "/auth/login/**",
                        "/auth/oauth/callback/**",
                        "/auth/refresh",
                        "/auth/me/status",
                        "/auth/signup/complete",
                    ).permitAll()

                    // 다른 모든 요청은 인증 필요
                    .anyRequest().authenticated()
            }
            .exceptionHandling { exceptions ->
                exceptions
                    .authenticationEntryPoint(customAuthenticationEntryPoint)
                    .accessDeniedHandler(customAccessDeniedHandler)
            }
            .addFilterBefore(devMockUserFilter, UsernamePasswordAuthenticationFilter::class.java)
            .addFilterAfter(jwtFilter, devMockUserFilter.javaClass)
            .addFilterAfter(pendingUserCheckFilter, jwtFilter.javaClass)
            .addFilterAfter(userIdMatchFilter, pendingUserCheckFilter.javaClass)
            .cors { it.configurationSource(corsConfigurationSource()) }
            .build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowedOrigins = listOf("http://localhost:3000")
        configuration.allowedMethods = listOf(
            HttpMethod.GET.name(),
            HttpMethod.POST.name(),
            HttpMethod.PUT.name(),
            HttpMethod.DELETE.name(),
            HttpMethod.OPTIONS.name()
        )
        configuration.allowedHeaders = listOf("*")
        configuration.exposedHeaders = listOf("Authorization", "Refresh-Token")
        configuration.allowCredentials = true
        configuration.maxAge = 3600L

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
}
