package com.ringgo.domain.test.entity

import jakarta.persistence.*

@Entity
@Table(name = "test_entity")
class TestEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val name: String,

    @Column
    val description: String? = null
)