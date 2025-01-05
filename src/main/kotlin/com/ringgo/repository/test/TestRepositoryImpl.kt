package com.ringgo.repository.test

import com.querydsl.jpa.impl.JPAQueryFactory
import com.ringgo.domain.test.entity.TestEntity
import com.ringgo.domain.test.entity.QTestEntity
import org.springframework.stereotype.Repository

@Repository
class TestRepositoryImpl(
    private val queryFactory: JPAQueryFactory
) : TestRepositoryCustom {
    override fun findByNameWithQuerydsl(name: String): List<TestEntity> {
        val testEntity = QTestEntity.testEntity
        return queryFactory
            .selectFrom(testEntity)
            .where(testEntity.name.eq(name))
            .fetch()
    }
}