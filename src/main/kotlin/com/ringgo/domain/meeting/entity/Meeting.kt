package com.ringgo.domain.meeting.entity

import com.ringgo.domain.meeting.entity.enums.MeetingStatus
import com.ringgo.domain.user.entity.User
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "meeting")
class Meeting(
    @Id
    @Column(columnDefinition = "BINARY(16)")
    val id: UUID = UUID.randomUUID(),  // AUTO_INCREMENT 대신 UUID 사용

    @Column(nullable = false, length = 50)
    var name: String,

    @Column(nullable = false)
    var icon: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: MeetingStatus = MeetingStatus.ACTIVE,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    val creator: User,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {

}