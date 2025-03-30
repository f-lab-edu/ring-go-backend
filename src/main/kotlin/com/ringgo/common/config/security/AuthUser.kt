package com.ringgo.common.config.security

import org.springframework.security.core.annotation.AuthenticationPrincipal

/**
 * @AuthenticationPrincipal에 표현식을 적용한 커스텀 어노테이션
 * JwtUserDetails에서 실제 User 엔티티를 추출
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@AuthenticationPrincipal(expression = "user")
annotation class AuthUser
