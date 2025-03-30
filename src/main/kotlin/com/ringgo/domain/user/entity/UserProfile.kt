package com.ringgo.domain.user.entity

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant
import java.util.*

@Entity
@Table(name = "user_profile")
@EntityListeners(AuditingEntityListener::class)
class UserProfile(
    @Id
    val userId: UUID,

    @Column(nullable = false, length = 50)
    var nickname: String,

    @Column(name = "image_path", length = 255)
    var imagePath: String?,

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    val user: User
) {
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    lateinit var createdAt: Instant

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    lateinit var updatedAt: Instant

    fun updateProfile(nickname: String?, imagePath: String?) {
        nickname?.let { this.nickname = it }
        imagePath?.let { this.imagePath = it }
    }
}