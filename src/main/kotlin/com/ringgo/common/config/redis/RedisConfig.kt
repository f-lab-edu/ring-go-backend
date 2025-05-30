package com.ringgo.common.config.redis

import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories

@Configuration
@EnableRedisRepositories(basePackages = []) // Redis 리포지토리를 사용하지 않음
@Import(value = [RedisRepositoriesAutoConfiguration::class])
class RedisConfig {
    // Redis는 캐싱 용도로만 사용
}
