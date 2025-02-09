package com.ringgo.domain.meeting.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import com.ringgo.common.exception.ApplicationException
import com.ringgo.common.exception.ErrorCode
import com.ringgo.common.fixture.TestUser
import com.ringgo.domain.meeting.dto.MeetingDto
import com.ringgo.domain.meeting.entity.Meeting
import com.ringgo.domain.meeting.entity.enums.MeetingStatus
import com.ringgo.domain.meeting.service.MeetingInviteService
import com.ringgo.domain.meeting.service.MeetingService
import com.ringgo.domain.member.entity.Member
import com.ringgo.domain.member.entity.enums.MemberRole
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.Instant
import java.util.*

@WebMvcTest(MeetingController::class)
@AutoConfigureMockMvc(addFilters = false)
class MeetingControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var meetingService: MeetingService

    @MockkBean
    private lateinit var meetingInviteService: MeetingInviteService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    // TestUser fixture 사용
    private val testUser = TestUser.create()


    @BeforeEach
    fun setUpMockAuth() {
        val authentication = UsernamePasswordAuthenticationToken(
            testUser,
            null,
            emptyList()
        )
        SecurityContextHolder.getContext().authentication = authentication
    }

    @AfterEach
    fun cleanup() {
        SecurityContextHolder.clearContext()
    }

    @Nested
    @DisplayName("모임 생성 API")
    inner class CreateMeeting {
        private val request = MeetingDto.Create.Request(
            name = "우리 가좍 소비 모임",
            icon = "group_icon.png"
        )

        @Test
        fun `모임 생성 성공시 201을 응답한다`() {
            // given
            val expectedResponse = MeetingDto.Create.Response(id = testUser.id)
            every { meetingService.create(request, any()) } returns expectedResponse

            // when & then
            mockMvc.perform(
                post("/api/v1/meeting")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isCreated)
                .andExpect(content().json(objectMapper.writeValueAsString(expectedResponse)))
                .andDo(print())

            verify(exactly = 1) { meetingService.create(request, any()) }
        }

        @Test
        fun `모임 이름이 없으면 400을 응답한다`() {
            // given
            val invalidRequest = request.copy(name = "")

            // when & then
            mockMvc.perform(
                post("/api/v1/meeting")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest))
            )
                .andExpect(status().isBadRequest)
                .andDo(print())
        }
    }

    @Nested
    @DisplayName("모임 목록 조회 API")
    inner class GetMyMeeting {
        @Test
        fun `내 모임 목록 조회 성공시 200을 응답한다`() {
            // given
            val expectedResponse = listOf(
                MeetingDto.Get.Response(
                    id = UUID.fromString("bc106686-d0e5-11ef-97fd-2cf05d34818a"),
                    name = "우리 가좍 소비 모임",
                    icon = "group_icon.png",
                    status = MeetingStatus.ACTIVE,
                    memberCount = 1,
                    createdAt = Instant.parse("2025-01-12T10:00:00Z"),
                    isCreator = true
                )
            )
            every { meetingService.getMyMeeting(any()) } returns expectedResponse

            // when & then
            mockMvc.perform(
                get("/api/v1/meeting")
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isOk)
                .andExpect(content().json(objectMapper.writeValueAsString(expectedResponse)))
                .andDo(print())

            verify(exactly = 1) { meetingService.getMyMeeting(any()) }
        }

        @Test
        fun `모임이 없을 경우 빈 리스트를 반환한다`() {
            // given
            every { meetingService.getMyMeeting(any()) } returns emptyList()

            // when & then
            mockMvc.perform(
                get("/api/v1/meeting")
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isOk)
                .andExpect(content().json(objectMapper.writeValueAsString(emptyList<MeetingDto.Get.Response>())))
                .andDo(print())

            verify(exactly = 1) { meetingService.getMyMeeting(any()) }
        }
    }


    @Nested
    @DisplayName("모임 상태 변경 API")
    inner class UpdateMeetingStatus {
        private val request = MeetingDto.UpdateStatus.Request(status = MeetingStatus.ENDED)

        @Test
        fun `모임 상태 변경 성공시 200을 응답한다`() {
            // given
            val expectedResponse = MeetingDto.UpdateStatus.Response(
                id = testUser.id,
                status = MeetingStatus.ENDED
            )
            every { meetingService.updateStatus(testUser.id, request, any()) } returns expectedResponse

            // when & then
            mockMvc.perform(
                patch("/api/v1/meeting/{id}/status", testUser.id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isOk)
                .andExpect(content().json(objectMapper.writeValueAsString(expectedResponse)))
                .andDo(print())

            verify(exactly = 1) { meetingService.updateStatus(testUser.id, request, any()) }
        }
    }

    @Nested
    @DisplayName("초대 링크 관련 API")
    inner class InviteLinkApis {
        private val meetingId = UUID.randomUUID()

        @Nested
        @DisplayName("초대 링크 생성")
        inner class CreateInviteLink {
            @Test
            fun `초대 링크 생성 성공시 201을 응답한다`() {
                // given
                val expectedResponse = MeetingDto.InviteLink.CreateResponse(
                    inviteUrl = "http://localhost:8080/invite/test-code",
                    expiredAt = "2025-02-09T10:00:00"
                )
                every { meetingInviteService.createInviteLink(meetingId, any()) } returns expectedResponse

                // when & then
                mockMvc.perform(
                    post("/api/v1/meeting/{id}/invite", meetingId)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                    .andExpect(status().isCreated)
                    .andExpect(content().json(objectMapper.writeValueAsString(expectedResponse)))
                    .andDo(print())

                verify(exactly = 1) { meetingInviteService.createInviteLink(meetingId, any()) }
            }

            @Test
            fun `존재하지 않는 모임의 초대 링크를 생성하면 404를 응답한다`() {
                // given
                every {
                    meetingInviteService.createInviteLink(
                        meetingId,
                        any()
                    )
                } throws ApplicationException(ErrorCode.MEETING_NOT_FOUND)

                // when & then
                mockMvc.perform(
                    post("/api/v1/meeting/{id}/invite", meetingId)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                    .andExpect(status().isNotFound)
                    .andDo(print())
            }

            @Test
            fun `모임 멤버 수가 초과되면 400을 응답한다`() {
                // given
                every {
                    meetingInviteService.createInviteLink(
                        meetingId,
                        any()
                    )
                } throws ApplicationException(ErrorCode.MEETING_MEMBER_LIMIT_EXCEEDED)

                // when & then
                mockMvc.perform(
                    post("/api/v1/meeting/{id}/invite", meetingId)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                    .andExpect(status().isBadRequest)
                    .andDo(print())
            }
        }

        @Nested
        @DisplayName("초대 코드로 모임 참여")
        inner class JoinWithInviteCode {

            @Test
            fun `모임 참여 성공시 200을 응답한다`() {
                // given
                val expectedResponse = MeetingDto.InviteLink.JoinResponse(
                    meetingId = UUID.randomUUID(),
                    meetingName = "우리 가좍 소비 모임"
                )

                every {
                    meetingInviteService.joinWithInviteCode(any(), any())
                } returns Member(
                    meeting = Meeting(
                        id = expectedResponse.meetingId,
                        name = expectedResponse.meetingName,
                        icon = "group_icon.png",
                        creator = testUser
                    ),
                    user = testUser,
                    role = MemberRole.MEMBER
                )

                // when & then
                mockMvc.perform(
                    post("/api/v1/meeting/invite/{code}", "test-code")
                        .contentType(MediaType.APPLICATION_JSON)
                )
                    .andExpect(status().isOk)
                    .andExpect(content().json(objectMapper.writeValueAsString(expectedResponse)))
                    .andDo(print())

                verify(exactly = 1) { meetingInviteService.joinWithInviteCode(any(), any()) }
            }

            @Test
            fun `만료된 초대 코드로 요청하면 410을 응답한다`() {
                // given
                every {
                    meetingInviteService.joinWithInviteCode(any(), any())
                } throws ApplicationException(ErrorCode.EXPIRED_INVITE_LINK)

                // when & then
                mockMvc.perform(
                    post("/api/v1/meeting/invite/{code}", "test-code")
                        .contentType(MediaType.APPLICATION_JSON)
                )
                    .andExpect(status().isGone)
                    .andDo(print())
            }

            @Test
            fun `이미 가입된 멤버가 요청하면 409를 응답한다`() {
                // given
                every {
                    meetingInviteService.joinWithInviteCode(any(), any())
                } throws ApplicationException(ErrorCode.ALREADY_JOINED_MEMBER)

                // when & then
                mockMvc.perform(
                    post("/api/v1/meeting/invite/{code}", "test-code")
                        .contentType(MediaType.APPLICATION_JSON)
                )
                    .andExpect(status().isConflict)
                    .andDo(print())
            }

            @Test
            fun `모임 멤버 수가 초과되면 400을 응답한다`() {
                // given
                every {
                    meetingInviteService.joinWithInviteCode(any(), any())
                } throws ApplicationException(ErrorCode.MEETING_MEMBER_LIMIT_EXCEEDED)

                // when & then
                mockMvc.perform(
                    post("/api/v1/meeting/invite/{code}", "test-code")
                        .contentType(MediaType.APPLICATION_JSON)
                )
                    .andExpect(status().isBadRequest)
                    .andDo(print())
            }
        }

        @Nested
        @DisplayName("모임원 관련 API")
        inner class MemberApis {
            @Nested
            @DisplayName("모임원 목록 조회 API")
            inner class GetMembers {
                private val meetingId = UUID.randomUUID()

                @Test
                fun `모임원 목록 조회 성공시 200을 응답한다`() {
                    // given
                    val expectedResponse = listOf(
                        MeetingDto.Member.Response(
                            id = UUID.randomUUID(),
                            userId = testUser.id,
                            name = testUser.name,
                            email = testUser.email,
                            role = MemberRole.CREATOR.name,
                            joinedAt = Instant.parse("2025-01-12T10:00:00Z")
                        )
                    )
                    every { meetingService.getMembers(meetingId, any()) } returns expectedResponse

                    // when & then
                    mockMvc.perform(
                        get("/api/v1/meeting/{id}/members", meetingId)
                            .contentType(MediaType.APPLICATION_JSON)
                    )
                        .andExpect(status().isOk)
                        .andExpect(content().json(objectMapper.writeValueAsString(expectedResponse)))
                        .andDo(print())

                    verify(exactly = 1) { meetingService.getMembers(meetingId, any()) }
                }

                @Test
                fun `모임원이 없을 경우 빈 리스트를 반환한다`() {
                    // given
                    every { meetingService.getMembers(meetingId, any()) } returns emptyList()

                    // when & then
                    mockMvc.perform(
                        get("/api/v1/meeting/{id}/members", meetingId)
                            .contentType(MediaType.APPLICATION_JSON)
                    )
                        .andExpect(status().isOk)
                        .andExpect(content().json(objectMapper.writeValueAsString(emptyList<MeetingDto.Member.Response>())))
                        .andDo(print())

                    verify(exactly = 1) { meetingService.getMembers(meetingId, any()) }
                }

                @Test
                fun `모임의 멤버가 아닌 경우 403을 응답한다`() {
                    // given
                    every {
                        meetingService.getMembers(meetingId, any())
                    } throws ApplicationException(ErrorCode.NOT_MEETING_MEMBER)

                    // when & then
                    mockMvc.perform(
                        get("/api/v1/meeting/{id}/members", meetingId)
                            .contentType(MediaType.APPLICATION_JSON)
                    )
                        .andExpect(status().isForbidden)
                        .andDo(print())
                }

                @Test
                fun `존재하지 않는 모임을 조회하면 404를 응답한다`() {
                    // given
                    every {
                        meetingService.getMembers(meetingId, any())
                    } throws ApplicationException(ErrorCode.MEETING_NOT_FOUND)

                    // when & then
                    mockMvc.perform(
                        get("/api/v1/meeting/{id}/members", meetingId)
                            .contentType(MediaType.APPLICATION_JSON)
                    )
                        .andExpect(status().isNotFound)
                        .andDo(print())
                }
            }

            @Nested
            @DisplayName("모임원 내보내기 API")
            inner class KickMember {
                private val meetingId = UUID.randomUUID()
                private val memberId = UUID.randomUUID()

                @Test
                fun `모임원 내보내기 성공시 204를 응답한다`() {
                    // given
                    every { meetingService.kickMember(meetingId, memberId, any()) } just runs

                    // when & then
                    mockMvc.perform(
                        delete("/api/v1/meeting/{meetingId}/members/{memberId}", meetingId, memberId)
                            .contentType(MediaType.APPLICATION_JSON)
                    )
                        .andExpect(status().isNoContent)
                        .andDo(print())

                    verify(exactly = 1) { meetingService.kickMember(meetingId, memberId, any()) }
                }

                @Test
                fun `모임의 생성자가 아닌 경우 403을 응답한다`() {
                    // given
                    every {
                        meetingService.kickMember(meetingId, memberId, any())
                    } throws ApplicationException(ErrorCode.NOT_MEETING_CREATOR)

                    // when & then
                    mockMvc.perform(
                        delete("/api/v1/meeting/{meetingId}/members/{memberId}", meetingId, memberId)
                            .contentType(MediaType.APPLICATION_JSON)
                    )
                        .andExpect(status().isForbidden)
                        .andDo(print())
                }

                @Test
                fun `자기 자신을 내보내려고 하면 400을 응답한다`() {
                    // given
                    every {
                        meetingService.kickMember(meetingId, memberId, any())
                    } throws ApplicationException(ErrorCode.CANNOT_KICK_SELF)

                    // when & then
                    mockMvc.perform(
                        delete("/api/v1/meeting/{meetingId}/members/{memberId}", meetingId, memberId)
                            .contentType(MediaType.APPLICATION_JSON)
                    )
                        .andExpect(status().isBadRequest)
                        .andDo(print())
                }

                @Test
                fun `모임에 속하지 않은 멤버를 내보내려고 하면 404를 응답한다`() {
                    // given
                    every {
                        meetingService.kickMember(meetingId, memberId, any())
                    } throws ApplicationException(ErrorCode.MEMBER_NOT_FOUND)

                    // when & then
                    mockMvc.perform(
                        delete("/api/v1/meeting/{meetingId}/members/{memberId}", meetingId, memberId)
                            .contentType(MediaType.APPLICATION_JSON)
                    )
                        .andExpect(status().isNotFound)
                        .andDo(print())
                }
            }
        }
    }
}