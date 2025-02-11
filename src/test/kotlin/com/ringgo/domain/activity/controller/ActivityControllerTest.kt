package com.ringgo.domain.activity.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import com.ringgo.common.exception.ApplicationException
import com.ringgo.common.exception.ErrorCode
import com.ringgo.common.fixture.TestUser
import com.ringgo.domain.activity.dto.ActivityDto
import com.ringgo.domain.activity.service.ActivityService
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.*

@WebMvcTest(ActivityController::class)
@AutoConfigureMockMvc(addFilters = false)
class ActivityControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var activityService: ActivityService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

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
    @DisplayName("활동 생성 API")
    inner class CreateActivity {
        private val meetingId = UUID.randomUUID()
        private val request = ActivityDto.Create.Request(
            meetingId = meetingId,
            type = "EXPENSE"
        )

        @Test
        fun `활동 생성 성공시 201을 응답한다`() {
            // given
            val expectedResponse = ActivityDto.Create.Response(id = 1L)
            every { activityService.create(request, any()) } returns expectedResponse

            // when & then
            mockMvc.perform(
                post("/api/v1/activity/$meetingId/EXPENSE")
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isCreated)
                .andExpect(content().json(objectMapper.writeValueAsString(expectedResponse)))
                .andDo(print())

            verify(exactly = 1) { activityService.create(request, any()) }
        }

        @Test
        fun `모임의 생성자가 아닌 경우 403을 응답한다`() {
            // given
            every { activityService.create(request, any()) } throws ApplicationException(ErrorCode.NOT_MEETING_CREATOR)

            // when & then
            mockMvc.perform(
                post("/api/v1/activity/$meetingId/EXPENSE")
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isForbidden)
                .andDo(print())
        }

        @Test
        fun `이미 동일한 타입의 활동이 존재하는 경우 409를 응답한다`() {
            // given
            every { activityService.create(request, any()) } throws ApplicationException(ErrorCode.DUPLICATE_ACTIVITY_TYPE)

            // when & then
            mockMvc.perform(
                post("/api/v1/activity/$meetingId/EXPENSE")
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isConflict)
                .andDo(print())
        }

        @Test
        fun `지원하지 않는 활동 타입인 경우 400을 응답한다`() {
            // given
            val invalidRequest = ActivityDto.Create.Request(
                meetingId = meetingId,
                type = "INVALID_TYPE"
            )
            every { activityService.create(invalidRequest, any()) } throws ApplicationException(ErrorCode.INVALID_ACTIVITY_TYPE)

            // when & then
            mockMvc.perform(
                post("/api/v1/activity/$meetingId/INVALID_TYPE")
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isBadRequest)
                .andDo(print())
        }
    }
}