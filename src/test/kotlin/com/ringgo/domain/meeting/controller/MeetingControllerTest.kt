package com.ringgo.domain.meeting.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import com.ringgo.common.exception.BusinessException
import com.ringgo.common.exception.ErrorCode
import com.ringgo.common.fixture.TestUser
import com.ringgo.domain.meeting.dto.MeetingDto
import com.ringgo.domain.meeting.entity.enums.MeetingStatus
import com.ringgo.domain.meeting.service.MeetingInviteService
import com.ringgo.domain.meeting.service.MeetingService
import io.mockk.every
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
import java.util.*

@WebMvcTest(MeetingController::class)
@AutoConfigureMockMvc(addFilters = false)
class MeetingControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var meetingService: MeetingService

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
                    createdAt = "2025-01-12T10:00:00",
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
    @DisplayName("초대 링크 생성 API")
    inner class CreateInviteLink {
        private val meetingId = UUID.randomUUID()

        @MockkBean
        private lateinit var meetingInviteService: MeetingInviteService

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
            } throws BusinessException(ErrorCode.MEETING_NOT_FOUND)

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
            } throws BusinessException(ErrorCode.MEETING_MEMBER_LIMIT_EXCEEDED)

            // when & then
            mockMvc.perform(
                post("/api/v1/meeting/{id}/invite", meetingId)
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isBadRequest)
                .andDo(print())
        }
    }

}