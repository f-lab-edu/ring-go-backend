package com.ringgo.repository.test

import com.ringgo.domain.test.entity.TestEntity
import org.springframework.data.jpa.repository.JpaRepository

interface TestRepository : JpaRepository<TestEntity, Long>, TestRepositoryCustom