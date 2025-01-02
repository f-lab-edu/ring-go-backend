package com.ringgo.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@Tag(name = "Test", description = "테스트용 API")
class TestController(
    private val redisTemplate: RedisTemplate<String, String>
) {
    @Operation(summary = "테스트 API", description = "서버 연결 테스트를 위한 API입니다.")
    @GetMapping("/test")
    fun test(): String {
        return "Hello from Ring-go Server!"
    }

    @Operation(summary = "Redis 테스트 API", description = "Redis 연결 테스트를 위한 API입니다.")
    @GetMapping("/redis/test")
    fun testRedis(): String {
        // Redis에 데이터 저장
        redisTemplate.opsForValue().set("test:key", "Hello from Redis!")

        // Redis에서 데이터 조회하여 반환
        return redisTemplate.opsForValue().get("test:key") ?: "No data found"
    }
}