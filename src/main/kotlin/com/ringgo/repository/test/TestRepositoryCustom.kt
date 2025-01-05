package com.ringgo.repository.test

import com.ringgo.domain.test.entity.TestEntity

interface TestRepositoryCustom {
    fun findByNameWithQuerydsl(name: String): List<TestEntity>
}