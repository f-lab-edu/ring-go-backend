package com.ringgo.common.config.security.oauth.provider

import com.fasterxml.jackson.databind.JsonNode
import com.ringgo.common.config.security.oauth.api.OAuthProperties
import com.ringgo.common.config.security.oauth.api.OAuthProvider
import com.ringgo.common.config.security.oauth.core.OAuthStateStore
import com.ringgo.common.exception.ApplicationException
import com.ringgo.common.exception.ErrorCode
import com.ringgo.domain.auth.dto.UserInfo
import com.ringgo.domain.user.entity.enums.Provider
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import java.util.*
import java.util.concurrent.TimeUnit

private val log = KotlinLogging.logger {}

/**
 * 구글 OAuth 제공자 구현체
 */
@Component
class GoogleOAuthProvider(
    private val OAuthProperties: OAuthProperties,
    private val webClient: WebClient,
    private val stateStore: OAuthStateStore
) : OAuthProvider {

    override fun getAuthorizationUrl(redirectUri: String): String {
        val props = OAuthProperties.google
        val state = UUID.randomUUID().toString()

        // 상태 저장
        stateStore.saveState("google", state, 10, TimeUnit.MINUTES)

        return "${props.authorizationUri}?response_type=code" +
                "&client_id=${props.clientId}" +
                "&redirect_uri=${OAuthProperties.callbackBaseUrl}${props.redirectUri}" +
                "&state=${state}" +
                "&scope=email%20profile" +  // 이메일과 프로필 정보 요청
                "&access_type=offline"      // 리프레시 토큰 요청
    }

    override fun getAccessToken(code: String): String {
        try {
            val props = OAuthProperties.google

            // 요청 본문 구성
            val formData = LinkedMultiValueMap<String, String>().apply {
                add("grant_type", "authorization_code")
                add("client_id", props.clientId)
                add("client_secret", props.clientSecret)
                add("code", code)
                add("redirect_uri", "${OAuthProperties.callbackBaseUrl}${props.redirectUri}")
            }

            log.debug { "${props.tokenUri}에 토큰 요청 파라미터: $formData" }

            // 액세스 토큰 요청
            val response = webClient.post()
                .uri(props.tokenUri)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(JsonNode::class.java)
                .block() ?: throw ApplicationException(ErrorCode.EXTERNAL_API_ERROR)

            log.debug { "토큰 응답: $response" }

            // 액세스 토큰 추출
            return response.get("access_token")?.asText()
                ?: throw ApplicationException(ErrorCode.INVALID_TOKEN)
        } catch (e: Exception) {
            log.error { "구글 액세스 토큰 획득 실패: ${e.message}" }
            throw ApplicationException(ErrorCode.EXTERNAL_API_ERROR)
        }
    }

    override fun getUserInfo(accessToken: String, includeProfileImage: Boolean): UserInfo {
        try {
            val props = OAuthProperties.google

            // 사용자 정보 요청
            val userInfo = webClient.get()
                .uri(props.userInfoUri)
                .header("Authorization", "Bearer $accessToken")
                .retrieve()
                .bodyToMono(JsonNode::class.java)
                .block() ?: throw ApplicationException(ErrorCode.EXTERNAL_API_ERROR)

            log.debug { "구글 사용자 정보 응답: $userInfo" }

            // 사용자 정보 추출
            val email = userInfo.path("email").asText()
            val name = userInfo.path("name").asText()
            val providerId = userInfo.path("sub").asText()

            // 프로필 이미지는 요청된 경우에만 추출
            val profileImageUrl = if (includeProfileImage) {
                userInfo.path("picture").asText("").takeIf { it.isNotBlank() }
            } else {
                null
            }

            return if (email.isBlank() || providerId.isBlank()) {
                log.error { "필수 구글 사용자 정보 누락 - 이메일: ${email.isBlank()}, 공급자ID: ${providerId.isBlank()}" }
                throw ApplicationException(ErrorCode.EXTERNAL_API_ERROR)
            } else {
                UserInfo(
                    providerId = providerId,
                    email = email,
                    name = name,
                    profileImageUrl = profileImageUrl,
                )
            }
        } catch (e: Exception) {
            log.error { "구글 사용자 정보 획득 실패: ${e.message}" }
            throw ApplicationException(ErrorCode.EXTERNAL_API_ERROR)
        }
    }

    override fun validateCallback(params: Map<String, String>, request: HttpServletRequest): Boolean {
        val state = params["state"] ?: return false
        return stateStore.validateState("google", state)
    }

    override fun getProviderType(): Provider = Provider.GOOGLE
}
