package com.ringgo.common.config.security.oauth.provider

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
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
 * 애플 OAuth 제공자 구현체
 */
@Component
class AppleOAuthProvider(
    private val OAuthProperties: OAuthProperties,
    private val webClient: WebClient,
    private val stateStore: OAuthStateStore,
    private val objectMapper: ObjectMapper
) : OAuthProvider {

    override fun getAuthorizationUrl(redirectUri: String): String {
        val props = OAuthProperties.apple
        val state = UUID.randomUUID().toString()

        // 상태 저장
        stateStore.saveState("apple", state, 10, TimeUnit.MINUTES)

        return "${props.authorizationUri}?response_type=code" +
                "&client_id=${props.clientId}" +
                "&redirect_uri=${OAuthProperties.callbackBaseUrl}${props.redirectUri}" +
                "&state=${state}" +
                "&scope=name%20email" +
                "&response_mode=form_post"
    }

    override fun getAccessToken(code: String): String {
        try {
            val props = OAuthProperties.apple

            // 요청 본문 구성
            val formData = LinkedMultiValueMap<String, String>().apply {
                add("grant_type", "authorization_code")
                add("client_id", props.clientId)
                add("client_secret", createClientSecret())
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

            // id_token에서 사용자 정보 추출
            val idToken = response.get("id_token")?.asText()
                ?: throw ApplicationException(ErrorCode.INVALID_TOKEN)

            // ID 토큰 반환
            return idToken

        } catch (e: Exception) {
            log.error { "애플 액세스 토큰 획득 실패: ${e.message}" }
            throw ApplicationException(ErrorCode.EXTERNAL_API_ERROR)
        }
    }

    override fun getUserInfo(accessToken: String, includeProfileImage: Boolean): UserInfo {
        try {
            val idToken = accessToken

            // ID 토큰 페이로드 부분 추출
            val payload = idToken.split(".")[1]
            val decodedPayload = String(Base64.getUrlDecoder().decode(payload))
            val claims = objectMapper.readTree(decodedPayload)

            log.debug { "애플 디코딩된 ID 토큰 클레임: $claims" }

            // 사용자 정보 추출
            val email = claims.path("email").asText()
            val providerId = claims.path("sub").asText()

            val name = "Apple User" // 기본값 사용
            val profileImageUrl = null

            return if (email.isBlank() || providerId.isBlank()) {
                log.error { "필수 애플 사용자 정보 누락 - 이메일: ${email.isBlank()}, 공급자ID: ${providerId.isBlank()}" }
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
            log.error { "애플 사용자 정보 획득 실패: ${e.message}" }
            throw ApplicationException(ErrorCode.EXTERNAL_API_ERROR)
        }
    }

    override fun validateCallback(params: Map<String, String>, request: HttpServletRequest): Boolean {
        val state = params["state"] ?: return false
        return stateStore.validateState("apple", state)
    }

    override fun getProviderType(): Provider = Provider.APPLE

    /**
     * 애플 클라이언트 시크릿 생성
     */
    private fun createClientSecret(): String {
        val props = OAuthProperties.apple
        val now = Date()
        val expirationTime = Date(now.time + 15 * 60 * 1000) // 15분 후 만료
        return "dummy_client_secret" // 실제 구현 필요
    }
}
