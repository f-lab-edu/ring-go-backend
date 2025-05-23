package com.ringgo.common.config.security

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfig(
    @Autowired(required = false)
    private val devMockUserFilter: DevMockUserFilter?
) {
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        val httpSecurity = http
            .csrf { it.disable() }
            .headers { headers ->
                headers.frameOptions { it.disable() }
            }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers(
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                    ).permitAll()
                    .anyRequest().authenticated()
            }
            .cors { it.disable() }

        // local 프로필에서만 devMockUserFilter 사용
        devMockUserFilter?.let { filter ->
            httpSecurity.addFilterBefore(filter, UsernamePasswordAuthenticationFilter::class.java)
        }

        return httpSecurity.build()
    }
}
