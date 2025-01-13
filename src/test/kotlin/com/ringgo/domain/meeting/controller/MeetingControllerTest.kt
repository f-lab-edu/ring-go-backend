package com.ringgo.domain.meeting.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import com.ringgo.common.dto.CommonResponse
import com.ringgo.domain.meeting.dto.MeetingDto
import com.ringgo.domain.meeting.service.MeetingService
import com.ringgo.domain.user.entity.User
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
import java.util.UUID

@WebMvcTest(MeetingController::class)
@AutoConfigureMockMvc(addFilters = false)
class MeetingControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var meetingService: MeetingService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private val testUser = User(
        id = UUID.fromString("bc0de3e8-d0e5-11ef-97fd-2cf05d34818a"),
        name = "전희진",
        email = "heejin@test.com",
        provider = "KAKAO",
        providerId = "kakao_123"
    )

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
            val serviceResponse = MeetingDto.Create.Response(id = testUser.id)
            val expectedResponse = CommonResponse.created(serviceResponse, "모임이 성공적으로 생성되었습니다.")

            every { meetingService.create(request, any()) } returns serviceResponse

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
            val serviceResponse = listOf(
                MeetingDto.Get.Response(
                    id = UUID.fromString("bc106686-d0e5-11ef-97fd-2cf05d34818a"),
                    name = "우리 가좍 소비 모임",
                    icon = "group_icon.png",
                    status = "ACTIVE",
                    memberCount = 1,
                    createdAt = "2025-01-12T10:00:00",
                    isCreator = true
                )
            )
            val expectedResponse = CommonResponse.success(serviceResponse)

            every { meetingService.getMyMeeting(any()) } returns serviceResponse

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
            val expectedResponse = CommonResponse.success(emptyList<MeetingDto.Get.Response>())

            every { meetingService.getMyMeeting(any()) } returns emptyList()

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
    }


    @Nested
    @DisplayName("모임 상태 변경 API")
    inner class UpdateMeetingStatus {
        private val request = MeetingDto.UpdateStatus.Request(status = "ENDED")

        @Test
        fun `모임 상태 변경 성공시 200을 응답한다`() {
            // given
            val expectedResponse = CommonResponse.success(Unit, "모임 상태가 성공적으로 변경되었습니다.")

            every { meetingService.updateStatus(testUser.id, request, any()) } returns Unit

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
}