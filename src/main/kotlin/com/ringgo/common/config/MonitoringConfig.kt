package com.ringgo.common.config

import io.github.oshai.kotlinlogging.KotlinLogging
import io.micrometer.core.aop.TimedAspect
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

private val log = KotlinLogging.logger {}

@Configuration
class MonitoringConfig {

    /**
     * 애플리케이션 공통 메트릭 태그 설정
     * (인프라 구축은 Terraform에서 담당)
     */
    @Bean
    fun metricsCommonTags(
        @Value("\${spring.profiles.active:local}") activeProfile: String
    ): MeterRegistryCustomizer<MeterRegistry> {

        log.info { "=== 애플리케이션 메트릭 설정 ===" }
        log.info { "Environment: $activeProfile" }
        log.info { "===============================" }

        return MeterRegistryCustomizer { registry ->
            registry.config()
                .commonTags(
                    "application", "ring-go-backend",
                    "environment", activeProfile,
                    "service", "api-server"
                )
        }
    }

    /**
     * @Timed 어노테이션 지원 (성능 모니터링용)
     */
    @Bean
    fun timedAspect(registry: MeterRegistry): TimedAspect {
        return TimedAspect(registry)
    }
}
