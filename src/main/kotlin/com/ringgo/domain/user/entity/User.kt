package com.ringgo.domain.user.entity

import com.ringgo.domain.member.entity.Member
import com.ringgo.domain.user.entity.enums.UserRole
import com.ringgo.domain.user.entity.enums.UserStatus
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant
import java.util.*

@Entity
@Table(
    name = "user",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_user_email",
            columnNames = ["email"]
        )
    ]
)
@EntityListeners(AuditingEntityListener::class)
class User(
    @Id
    @Column(columnDefinition = "BINARY(16)")
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false, length = 320)
    val email: String,

    @Column(nullable = false, length = 50)
    val name: String,

    @Column(name = "refresh_token", length = 512)
    var refreshToken: String?,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var role: UserRole = UserRole.NORMAL,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: UserStatus = UserStatus.ACTIVE
) {
    @OneToOne(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    var profile: UserProfile? = null

    @OneToMany(mappedBy = "user")
    val members: MutableList<Member> = mutableListOf()

    @OneToMany(mappedBy = "user")
    val connections: MutableList<UserConnection> = mutableListOf()

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    lateinit var createdAt: Instant

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    lateinit var updatedAt: Instant

    constructor(email: String, name: String, role: UserRole, status: UserStatus) : this(
        email = email,
        name = name,
        refreshToken = null,
        role = role,
        status = status
    )

    fun updateRefreshToken(refreshToken: String?) {
        this.refreshToken = refreshToken
    }

    fun deactivate() {
        this.status = UserStatus.INACTIVE
    }

    fun activate() {
        this.status = UserStatus.ACTIVE
    }
}
