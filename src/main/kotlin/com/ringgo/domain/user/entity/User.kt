package com.ringgo.domain.user.entity

import com.ringgo.domain.member.entity.Member
import com.ringgo.domain.user.entity.enums.UserRole
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant
import java.util.*

@Entity
@Table(name = "user")
@EntityListeners(AuditingEntityListener::class)
class User(
    @Id
    @Column(columnDefinition = "BINARY(16)")
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false, length = 50)
    var name: String,

    @Column(nullable = false, unique = true, length = 320)
    val email: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    var role: UserRole = UserRole.NORMAL,

    @Column(nullable = false)
    val provider: String,

    @Column(name = "provider_id", nullable = false)
    val providerId: String,
) {
    @OneToMany(mappedBy = "user")
    val members: MutableList<Member> = mutableListOf()

    @CreatedDate
    @Column(nullable = false, updatable = false)
    lateinit var createdAt: Instant

    @LastModifiedDate
    @Column(nullable = false)
    lateinit var updatedAt: Instant
}