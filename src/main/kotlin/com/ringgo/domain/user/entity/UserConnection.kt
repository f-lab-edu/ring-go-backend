package com.ringgo.domain.user.entity

import com.ringgo.domain.user.entity.enums.Provider
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant
import java.util.*

@Entity
@Table(
    name = "user_connection",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_user_connection_provider",
            columnNames = ["provider", "provider_id"]
        )
    ]
)
@EntityListeners(AuditingEntityListener::class)
class UserConnection(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "user_id", nullable = false, columnDefinition = "BINARY(16)")
    val userId: UUID,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    val user: User? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    val provider: Provider,

    @Column(name = "provider_id", nullable = false, length = 255)
    val providerId: String
) {
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    lateinit var createdAt: Instant

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    lateinit var updatedAt: Instant
}
