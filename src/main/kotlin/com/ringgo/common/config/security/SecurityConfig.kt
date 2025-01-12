package com.ringgo.common.config.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val devMockUserFilter: DevMockUserFilter  // 의존성 주입
) {
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .csrf { it.disable() }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()  // Swagger 관련 경로만 명시적 허용
                    .anyRequest().authenticated()  // 나머지는 인증 필요
            }
            .addFilterBefore(devMockUserFilter, UsernamePasswordAuthenticationFilter::class.java)
            .cors { it.disable() }
            .build()
    }
}