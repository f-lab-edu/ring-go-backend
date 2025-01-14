package com.ringgo.domain.meeting.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import com.ringgo.common.dto.CommonResponse
import com.ringgo.common.fixture.TestUser
import com.ringgo.domain.meeting.dto.MeetingDto
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
}