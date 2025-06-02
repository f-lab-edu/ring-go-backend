package com.ringgo.common.config.docs

import io.github.oshai.kotlinlogging.KotlinLogging
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

private val log = KotlinLogging.logger {}

@Configuration
class SwaggerConfig(
    @Value("\${swagger.url:\${SERVER_BASE_URL:http://localhost:8080}}") private val url: String,
    @Value("\${swagger.description}") private val description: String,
    @Value("\${spring.profiles.active:local}") private val activeProfile: String
) {

    @Bean
    fun openAPI(): OpenAPI {
        log.info { "=== Swagger Config ===" }
        log.info { "URL: $url" }
        log.info { "Description: $description" }
        log.info { "Profile: $activeProfile" }
        log.info { "======================" }

        return OpenAPI()
            .info(createApiInfo())
            .servers(listOf(Server().url(url).description(description)))
            .components(createComponents())
    }

    private fun createApiInfo(): Info {
        val description = when (activeProfile) {
            "local" -> """
                ## 🏠 로컬 개발 환경
                
                ### 인증
                - **자동 MockUser**: 신짱구 계정으로 자동 로그인
                - **추가 인증 불필요**: 바로 API 테스트 가능
                
                ### 데이터베이스  
                - **로컬 H2** 또는 **원격 MySQL** 사용 가능
                - 설정에 따라 자동 선택
                
                ### 개발 도구
                - **CORS 테스트**: 브라우저 요청 확인
                - **헬스체크**: 서버 상태 모니터링
                - **전체 API**: 모든 개발용 API 포함
            """.trimIndent()

            "dev" -> """
                ## 🚀 개발 서버 환경
                
                ### 인증 시스템
                - **자동 MockUser**: 개발자 계정으로 자동 로그인  
                - **다중 사용자 테스트**: 개발용 로그인 API 활용
                
                ### 인프라
                - **원격 MySQL**: 개발 서버 데이터베이스 연동
                - **Redis 캐시**: 실제 캐시 시스템 테스트
                - **Kafka**: 메시지 큐 시스템 연동
                
                ### 팀 협업 도구
                - **공통 환경**: 모든 개발자가 동일한 데이터로 테스트
                - **API 문서**: 실시간 API 스펙 확인
                - **모니터링**: 서버 상태 및 성능 확인
                
                ### 다양한 사용자로 테스트
                1. `POST /v1/dev/login/admin` - 관리자 권한
                2. `POST /v1/dev/login/user` - 일반 사용자  
                3. `POST /v1/dev/login/test` - 테스트 계정
                4. `GET /v1/dev/me` - 현재 로그인 사용자 확인
            """.trimIndent()

            else -> """
                ## Ring-Go 내부 API 문서
                
                팀 내부 개발 및 테스트용 API 문서입니다.
                모든 개발용 도구와 시스템 API가 포함되어 있습니다.
            """.trimIndent()
        }

        return Info()
            .title("Ring-Go Internal API")
            .description(description)
            .version("1.0.0")
    }

    private fun createComponents(): Components {
        return Components()
            .addSecuritySchemes("bearer-key",
                SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("향후 JWT 인증 시스템 구현 예정 (현재는 MockUser 자동 인증)")
            )
    }
}
