package com.ringgo.common.config.security.oauth.api

import com.ringgo.common.config.security.oauth.OAuth2ClientProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "oauth2")
class OAuthProperties {
    lateinit var callbackBaseUrl: String
    val kakao = OAuth2ClientProperties()
    val naver = OAuth2ClientProperties()
    val google = OAuth2ClientProperties()
    val apple = OAuth2ClientProperties()
}
