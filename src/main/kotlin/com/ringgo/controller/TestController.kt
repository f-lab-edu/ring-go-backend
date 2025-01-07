package com.ringgo.controller

import com.ringgo.domain.test.entity.TestEntity
import com.ringgo.repository.test.TestRepository
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.web.bind.annotation.*
import java.util.concurrent.CompletableFuture

@RestController
@Tag(name = "Test", description = "테스트용 API")
class TestController(
    private val redisTemplate: RedisTemplate<String, String>,
    private val testRepository: TestRepository,
    private val kafkaTemplate: KafkaTemplate<String, String>

) {
    @Operation(summary = "테스트 API", description = "서버 연결 테스트를 위한 API입니다.")
    @GetMapping("/test")
    fun test(): String {
        return "Hello from Ring-go Server!"
    }

    @Operation(summary = "Redis 테스트 API", description = "Redis 연결 테스트를 위한 API입니다.")
    @GetMapping("/redis/test")
    fun testRedis(): String {
        redisTemplate.opsForValue().set("test:key", "Hello from Redis!")
        return redisTemplate.opsForValue().get("test:key") ?: "No data found"
    }

    @Operation(summary = "JPA 테스트 API", description = "JPA 연결 테스트를 위한 API입니다.")
    @PostMapping("/jpa/test")
    fun testJpa(@RequestParam name: String, @RequestParam description: String?): TestEntity {
        val testEntity = TestEntity(
            name = name,
            description = description
        )
        return testRepository.save(testEntity)
    }

    @Operation(summary = "JPA 조회 테스트 API", description = "JPA 조회 테스트를 위한 API입니다.")
    @GetMapping("/jpa/test")
    fun getAllTestEntities(): List<TestEntity> {
        return testRepository.findAll()
    }

    @Operation(summary = "QueryDSL 테스트 API", description = "QueryDSL 테스트를 위한 API입니다.")
    @GetMapping("/querydsl/test")
    fun testQuerydsl(@RequestParam name: String): List<TestEntity> {
        return testRepository.findByNameWithQuerydsl(name)
    }

    @Operation(summary = "Kafka Producer 테스트 API", description = "Kafka 메시지 전송 테스트를 위한 API입니다.")
    @PostMapping("/kafka/test")
    fun testKafkaProducer(
        @RequestParam(defaultValue = "test-topic") topic: String,
        @RequestParam(required = true) message: String
    ): CompletableFuture<String> {
        return kafkaTemplate.send(topic, message)
            .thenApply { result ->
                "Message sent successfully to topic: $topic, offset: ${result.recordMetadata.offset()}"
            }
            .exceptionally { e ->
                "Failed to send message: ${e.message}"
            }
    }
}