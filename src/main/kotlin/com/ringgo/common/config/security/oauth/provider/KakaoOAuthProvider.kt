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
 * 카카오 OAuth 제공자 구현체
 */
@Component
class KakaoOAuthProvider(
    private val OAuthProperties: OAuthProperties,
    private val webClient: WebClient,
    private val stateStore: OAuthStateStore
) : OAuthProvider {

    override fun getAuthorizationUrl(redirectUri: String): String {
        val props = OAuthProperties.kakao
        val state = UUID.randomUUID().toString()

        // 상태 저장
        stateStore.saveState("kakao", state, 10, TimeUnit.MINUTES)

        return "${props.authorizationUri}?response_type=code" +
                "&client_id=${props.clientId}" +
                "&redirect_uri=${OAuthProperties.callbackBaseUrl}${props.redirectUri}" +
                "&state=${state}" +
                "&scope=account_email,profile_nickname"
    }

    override fun getAccessToken(code: String): String {
        try {
            val props = OAuthProperties.kakao

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
            log.error { "카카오 액세스 토큰 획득 실패: ${e.message}" }
            throw ApplicationException(ErrorCode.EXTERNAL_API_ERROR)
        }
    }

    override fun getUserInfo(accessToken: String, includeProfileImage: Boolean): UserInfo {
        try {
            val props = OAuthProperties.kakao

            // 사용자 정보 요청
            val userInfo = webClient.get()
                .uri(props.userInfoUri)
                .header("Authorization", "Bearer $accessToken")
                .retrieve()
                .bodyToMono(JsonNode::class.java)
                .block() ?: throw ApplicationException(ErrorCode.EXTERNAL_API_ERROR)

            log.debug { "카카오 사용자 정보 응답: $userInfo" }

            // 사용자 정보 추출
            val email = userInfo.path("kakao_account").path("email").asText()
            val providerId = userInfo.path("id").asText()

            // 프로필 이미지는 요청된 경우에만 추출
            val profileImageUrl = if (includeProfileImage) {
                userInfo.path("properties").path("profile_image").asText("").takeIf { it.isNotBlank() }
            } else {
                null
            }

            // 닉네임이 없는 경우 대체 값 사용
            val name = userInfo.path("properties").path("nickname").asText().takeIf { it.isNotBlank() }
                ?: email.split("@").first().takeIf { it.isNotBlank() }
                ?: "Kakao User"

            return UserInfo(
                providerId = providerId,
                email = email,
                name = name,
                profileImageUrl = profileImageUrl,
            )
        } catch (e: Exception) {
            log.error { "카카오 사용자 정보 획득 실패: ${e.message}" }
            throw ApplicationException(ErrorCode.EXTERNAL_API_ERROR)
        }
    }

    override fun validateCallback(params: Map<String, String>, request: HttpServletRequest): Boolean {
        val state = params["state"]
        return state == null || stateStore.validateState("kakao", state)
    }

    override fun getProviderType(): Provider = Provider.KAKAO
}
