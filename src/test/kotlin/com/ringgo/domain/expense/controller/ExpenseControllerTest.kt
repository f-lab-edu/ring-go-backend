package com.ringgo.domain.expense.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import com.ringgo.common.exception.ApplicationException
import com.ringgo.common.exception.ErrorCode
import com.ringgo.common.fixture.TestUser
import com.ringgo.domain.expense.dto.ExpenseDto
import com.ringgo.domain.expense.entity.enums.ExpenseCategory
import com.ringgo.domain.expense.service.ExpenseService
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.math.BigDecimal
import java.time.Instant

@WebMvcTest(ExpenseController::class)
@AutoConfigureMockMvc(addFilters = false)
class ExpenseControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var expenseService: ExpenseService

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
    @DisplayName("지출 생성 API")
    inner class CreateExpense {
        private val request = ExpenseDto.Create.Request(
            activityId = 1L,
            name = "갓덴스시",
            amount = BigDecimal("10000.00"),
            category = ExpenseCategory.FOOD,
            description = "어제 야근하느라 힘들어서 진짜 나한테 보상을 주고 싶었음.. 그래서 점심에 초밥 사먹었어요. ㅋㅋ",
            expenseDate = Instant.parse("2025-02-14T12:00:00Z")
        )

        @Test
        fun `지출 생성 성공시 201을 응답한다`() {
            // given
            val expectedResponse = ExpenseDto.Create.Response(
                id = 1L,
                createdAt = Instant.parse("2025-02-14T12:00:00Z")
            )
            every { expenseService.create(request, any()) } returns expectedResponse

            // when & then
            mockMvc.perform(
                post("/api/v1/expense")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isCreated)
                .andExpect(content().json(objectMapper.writeValueAsString(expectedResponse)))
                .andDo(print())

            verify(exactly = 1) { expenseService.create(request, any()) }
        }

        @Test
        fun `유효하지 않은 요청시 400을 응답한다`() {
            // given
            val invalidRequest = request.copy(name = "")

            // when & then
            mockMvc.perform(
                post("/api/v1/expense")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest))
            )
                .andExpect(status().isBadRequest)
                .andDo(print())
        }

        @Test
        fun `활동을 찾을 수 없으면 404를 응답한다`() {
            // given
            every { expenseService.create(request, any()) } throws ApplicationException(ErrorCode.ACTIVITY_NOT_FOUND)

            // when & then
            mockMvc.perform(
                post("/api/v1/expense")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isNotFound)
                .andDo(print())
        }
    }

    @Nested
    @DisplayName("지출 수정 API")
    inner class UpdateExpense {
        private val request = ExpenseDto.Update.Request(
            name = "갓덴스시",
            amount = BigDecimal("66000.00"),
            category = ExpenseCategory.FOOD,
            description = "어제 야근하느라 힘들어서 진짜 나한테 보상을 주고 싶었음.. 그래서 점심에 초밥 사먹었어요. ㅋㅋ",
            expenseDate = Instant.parse("2025-02-14T12:00:00Z")
        )

        @Test
        fun `지출 수정 성공시 200을 응답한다`() {
            // given
            val expectedResponse = ExpenseDto.Update.Response(
                id = 1L,
                updatedAt = Instant.parse("2025-02-14T12:00:00Z")
            )
            every { expenseService.update(1L, request, any()) } returns expectedResponse

            // when & then
            mockMvc.perform(
                patch("/api/v1/expense/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isOk)
                .andExpect(content().json(objectMapper.writeValueAsString(expectedResponse)))
                .andDo(print())

            verify(exactly = 1) { expenseService.update(1L, request, any()) }
        }

        @Test
        fun `존재하지 않는 지출을 수정하면 404를 응답한다`() {
            // given
            every { expenseService.update(999L, request, any()) } throws
                    ApplicationException(ErrorCode.EXPENSE_NOT_FOUND)

            // when & then
            mockMvc.perform(
                patch("/api/v1/expense/999")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isNotFound)
                .andDo(print())
        }

        @Test
        fun `권한이 없는 경우 403을 응답한다`() {
            // given
            every { expenseService.update(1L, request, any()) } throws
                    ApplicationException(ErrorCode.NOT_EXPENSE_CREATOR)

            // when & then
            mockMvc.perform(
                patch("/api/v1/expense/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isForbidden)
                .andDo(print())
        }
    }

    @Nested
    @DisplayName("지출 수정 API")
    inner class UpdateExpense {
        private val fullRequest = ExpenseDto.Update.Request(
            name = "갓덴스시",
            amount = BigDecimal("66000.00"),
            category = ExpenseCategory.FOOD,
            description = "어제 야근하느라 힘들어서 진짜 나한테 보상을 주고 싶었음.. 그래서 점심에 초밥 사먹었어요. ㅋㅋ",
            expenseDate = Instant.parse("2025-02-14T12:00:00Z")
        )

        @Test
        fun `전체 필드 수정 성공시 200을 응답한다`() {
            // given
            val expectedResponse = ExpenseDto.Update.Response(
                id = 1L,
                updatedAt = Instant.parse("2025-02-14T12:00:00Z")
            )
            every { expenseService.update(1L, fullRequest, any()) } returns expectedResponse

            // when & then
            mockMvc.perform(
                patch("/api/v1/expense/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(fullRequest))
            )
                .andExpect(status().isOk)
                .andExpect(content().json(objectMapper.writeValueAsString(expectedResponse)))
                .andDo(print())

            verify(exactly = 1) { expenseService.update(1L, fullRequest, any()) }
        }

        @Test
        fun `금액만 수정 성공시 200을 응답한다`() {
            // given
            val partialRequest = ExpenseDto.Update.Request(
                amount = BigDecimal("30000.00")
            )
            val expectedResponse = ExpenseDto.Update.Response(
                id = 1L,
                updatedAt = Instant.parse("2025-02-14T12:00:00Z")
            )
            every { expenseService.update(1L, partialRequest, any()) } returns expectedResponse

            // when & then
            mockMvc.perform(
                patch("/api/v1/expense/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(partialRequest))
            )
                .andExpect(status().isOk)
                .andExpect(content().json(objectMapper.writeValueAsString(expectedResponse)))
                .andDo(print())
        }

        @Test
        fun `설명만 수정 성공시 200을 응답한다`() {
            // given
            val partialRequest = ExpenseDto.Update.Request(
                description = "점심에 초밥 사먹었어요"
            )
            val expectedResponse = ExpenseDto.Update.Response(
                id = 1L,
                updatedAt = Instant.parse("2025-02-14T12:00:00Z")
            )
            every { expenseService.update(1L, partialRequest, any()) } returns expectedResponse

            // when & then
            mockMvc.perform(
                patch("/api/v1/expense/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(partialRequest))
            )
                .andExpect(status().isOk)
                .andExpect(content().json(objectMapper.writeValueAsString(expectedResponse)))
                .andDo(print())
        }

        @Test
        fun `존재하지 않는 지출을 수정하면 404를 응답한다`() {
            // given
            every { expenseService.update(999L, fullRequest, any()) } throws
                    ApplicationException(ErrorCode.EXPENSE_NOT_FOUND)

            // when & then
            mockMvc.perform(
                patch("/api/v1/expense/999")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(fullRequest))
            )
                .andExpect(status().isNotFound)
                .andDo(print())
        }

        @Test
        fun `지출 작성자가 아닌 경우 403을 응답한다`() {
            // given
            every { expenseService.update(1L, fullRequest, any()) } throws
                    ApplicationException(ErrorCode.NOT_EXPENSE_CREATOR)

            // when & then
            mockMvc.perform(
                patch("/api/v1/expense/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(fullRequest))
            )
                .andExpect(status().isForbidden)
                .andDo(print())
        }

        @Test
        fun `지출명이 입력된 경우 빈 값이면 400을 응답한다`() {
            // given
            val invalidRequest = ExpenseDto.Update.Request(
                name = ""
            )

            // when & then
            mockMvc.perform(
                patch("/api/v1/expense/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest))
            )
                .andExpect(status().isBadRequest)
                .andDo(print())
        }

        @Test
        fun `금액이 입력된 경우 0 이하면 400을 응답한다`() {
            // given
            val invalidRequest = ExpenseDto.Update.Request(
                amount = BigDecimal.ZERO
            )

            // when & then
            mockMvc.perform(
                patch("/api/v1/expense/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest))
            )
                .andExpect(status().isBadRequest)
                .andDo(print())
        }

        @Test
        fun `지출명이 입력된 경우 20자를 초과하면 400을 응답한다`() {
            // given
            val invalidRequest = ExpenseDto.Update.Request(
                name = "일".repeat(21)
            )

            // when & then
            mockMvc.perform(
                patch("/api/v1/expense/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest))
            )
                .andExpect(status().isBadRequest)
                .andDo(print())
        }

        @Test
        fun `설명이 입력된 경우 200자를 초과하면 400을 응답한다`() {
            // given
            val invalidRequest = ExpenseDto.Update.Request(
                description = "가".repeat(201)
            )

            // when & then
            mockMvc.perform(
                patch("/api/v1/expense/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest))
            )
                .andExpect(status().isBadRequest)
                .andDo(print())
        }

        @Test
        fun `비활성화된 활동의 지출을 수정하면 400을 응답한다`() {
            // given
            every { expenseService.update(1L, fullRequest, any()) } throws
                    ApplicationException(ErrorCode.INACTIVE_ACTIVITY)

            // when & then
            mockMvc.perform(
                patch("/api/v1/expense/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(fullRequest))
            )
                .andExpect(status().isBadRequest)
                .andDo(print())
        }
    }
}