package com.ringgo.common.config.security.oauth

/**
 * OAuth2 클라이언트 구성 속성
 */
class OAuth2ClientProperties {
    var clientId: String = ""
    var clientSecret: String = ""
    var redirectUri: String = ""
    var authorizationUri: String = ""
    var tokenUri: String = ""
    var userInfoUri: String = ""
}
