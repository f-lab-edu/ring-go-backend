package com.ringgo.common.config.security

import org.springframework.security.core.annotation.AuthenticationPrincipal

/**
 * @AuthenticationPrincipal 을 활용한 커스텀 어노테이션
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@AuthenticationPrincipal
annotation class AuthUser
