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
            expenseDate = Instant.parse("2025-02-14")
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
            expenseDate = Instant.parse("2025-02-14")
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
    @DisplayName("지출 삭제 API")
    inner class DeleteExpense {
        private val expenseId = 1L

        @Test
        fun `모임원의 지출 삭제 요청시 204를 응답한다`() {
            // given
            every { expenseService.delete(expenseId, testUser) } just runs

            // when & then
            mockMvc.perform(delete("/api/v1/expense/$expenseId"))
                .andExpect(status().isNoContent)
                .andDo(print())

            verify(exactly = 1) { expenseService.delete(expenseId, testUser) }
        }

        @Test
        fun `삭제 대상 지출이 없는 경우 404를 응답한다`() {
            // given
            every { expenseService.delete(expenseId, testUser) } throws
                    ApplicationException(ErrorCode.EXPENSE_NOT_FOUND)

            // when & then
            mockMvc.perform(delete("/api/v1/expense/$expenseId"))
                .andExpect(status().isNotFound)
                .andDo(print())

            verify(exactly = 1) { expenseService.delete(expenseId, testUser) }
        }

        @Test
        fun `다른 모임원의 지출 삭제 요청시 403을 응답한다`() {
            // given
            every { expenseService.delete(expenseId, testUser) } throws
                    ApplicationException(ErrorCode.NOT_EXPENSE_CREATOR)

            // when & then
            mockMvc.perform(delete("/api/v1/expense/$expenseId"))
                .andExpect(status().isForbidden)
                .andDo(print())

            verify(exactly = 1) { expenseService.delete(expenseId, testUser) }
        }

        @Test
        fun `종료된 활동의 지출 삭제 요청시 400을 응답한다`() {
            // given
            every { expenseService.delete(expenseId, testUser) } throws
                    ApplicationException(ErrorCode.INACTIVE_ACTIVITY)

            // when & then
            mockMvc.perform(delete("/api/v1/expense/$expenseId"))
                .andExpect(status().isBadRequest)
                .andDo(print())

            verify(exactly = 1) { expenseService.delete(expenseId, testUser) }
        }
    }

    @Nested
    @DisplayName("지출 목록 조회 API")
    inner class ListExpense {
        private val activityId = 1L
        private val request = ExpenseDto.Get.Request(
            activityId = activityId,
            sortOrder = false
        )

        private val now = Instant.parse("2025-02-14T00:00:00Z")
        private val expenseItems = listOf(
            ExpenseDto.Get.ExpenseItem(
                id = 1L,
                name = "갓덴스시",
                amount = BigDecimal("15000.00"),
                category = ExpenseCategory.FOOD,
                description = "어제 야근하느라 힘들어서 진짜 나한테 보상을 주고 싶었음..",
                createdAt = Instant.parse("2025-01-13T12:00:00Z")
            ),
            ExpenseDto.Get.ExpenseItem(
                id = 2L,
                name = "택시비",
                amount = BigDecimal("15000.00"),
                category = ExpenseCategory.TRANSPORT,
                description = "늦게까지 일하다가 택시타고 귀가",
                createdAt = Instant.parse("2025-01-13T13:00:00Z")
            )
        )

        private val userExpense = ExpenseDto.Get.UserExpense(
            userId = testUser.id,
            userName = testUser.name,
            expenses = expenseItems,
            totalAmount = BigDecimal("30000.00")
        )

        @Test
        fun `지출 목록 조회 성공시 200을 응답한다`() {
            // given
            val expectedResponse = ExpenseDto.Get.Response(
                dailyExpenses = listOf(
                    ExpenseDto.Get.DailyExpense(
                        date = now,
                        userExpenses = listOf(userExpense)
                    )
                )
            )
            every { expenseService.list(request, any()) } returns expectedResponse

            // when & then
            mockMvc.perform(
                get("/api/v1/expense")
                    .param("activityId", activityId.toString())
                    .param("sortOrder", "false")
            )
                .andExpect(status().isOk)
                .andExpect(content().json(objectMapper.writeValueAsString(expectedResponse)))
                .andDo(print())

            verify(exactly = 1) { expenseService.list(request, any()) }
        }

        @Test
        fun `활동을 찾을 수 없으면 404를 응답한다`() {
            // given
            every { expenseService.list(request, any()) } throws
                    ApplicationException(ErrorCode.ACTIVITY_NOT_FOUND)

            // when & then
            mockMvc.perform(
                get("/api/v1/expense")
                    .param("activityId", activityId.toString())
            )
                .andExpect(status().isNotFound)
                .andDo(print())

            verify(exactly = 1) { expenseService.list(request, any()) }
        }

        @Test
        fun `모임 멤버가 아니면 403을 응답한다`() {
            // given
            every { expenseService.list(request, any()) } throws
                    ApplicationException(ErrorCode.NOT_MEETING_MEMBER)

            // when & then
            mockMvc.perform(
                get("/api/v1/expense")
                    .param("activityId", activityId.toString())
            )
                .andExpect(status().isForbidden)
                .andDo(print())

            verify(exactly = 1) { expenseService.list(request, any()) }
        }

        @Test
        fun `활동 ID 없이 요청하면 400을 응답한다`() {
            // when & then
            mockMvc.perform(get("/api/v1/expense"))
                .andExpect(status().isBadRequest)
                .andDo(print())
        }
    }

    @Nested
    @DisplayName("지출 검색 API")
    inner class SearchExpense {
        private val activityId = 1L
        private val baseExpenseItems = listOf(
            ExpenseDto.Search.ExpenseItem(
                id = 1L,
                name = "점심 식사",
                amount = BigDecimal("15000.00"),
                category = ExpenseCategory.FOOD,
                description = "팀원들과 점심 식사",
                createdAt = Instant.parse("2024-02-14T12:00:00Z")
            ),
            ExpenseDto.Search.ExpenseItem(
                id = 2L,
                name = "저녁 회식",
                amount = BigDecimal("25000.00"),
                category = ExpenseCategory.FOOD,
                description = "팀 회식",
                createdAt = Instant.parse("2024-02-14T18:00:00Z")
            )
        )

        private val baseUserExpense = ExpenseDto.Search.UserExpense(
            userId = testUser.id,
            userName = testUser.name,
            expenses = baseExpenseItems,
            totalAmount = BigDecimal("40000.00")
        )

        private val baseExpenseDate = LocalDate.of(2024, 2, 14)

        private val baseResponse = ExpenseDto.Search.Response(
            dailyExpenses = listOf(
                ExpenseDto.Search.DailyExpense(
                    date = baseExpenseDate,
                    userExpenses = listOf(baseUserExpense)
                )
            ),
            metadata = ExpenseDto.Search.SearchMetadata(
                totalElements = 2,
                totalPages = 1,
                currentPage = 0,
                pageSize = 20,
                totalAmount = BigDecimal("40000.00")
            )
        )

        @Test
        fun `키워드 없이 검색하면 전체 지출 목록을 반환한다`() {
            // given
            val request = ExpenseDto.Search.Request(
                activityId = activityId,
                keyword = null,
                startDate = null,
                endDate = null,
                sortOrder = false,
                page = 0,
                size = 20
            )
            every { expenseService.search(request, any()) } returns baseResponse

            // when & then
            mockMvc.perform(
                get("/api/v1/expense/search")
                    .param("activityId", request.activityId.toString())
                    .param("sortOrder", request.sortOrder.toString())
                    .param("page", request.page.toString())
                    .param("size", request.size.toString())
            )
                .andExpect(status().isOk)
                .andExpect(content().json(objectMapper.writeValueAsString(baseResponse)))
                .andDo(print())

            verify(exactly = 1) { expenseService.search(request, any()) }
        }

        @Test
        fun `키워드로 검색하면 통합 검색 결과를 반환한다`() {
            // given
            val request = ExpenseDto.Search.Request(
                activityId = activityId,
                keyword = "회식",
                startDate = null,
                endDate = null,
                sortOrder = false,
                page = 0,
                size = 20
            )
            every { expenseService.search(request, any()) } returns baseResponse

            // when & then
            mockMvc.perform(
                get("/api/v1/expense/search")
                    .param("activityId", request.activityId.toString())
                    .param("keyword", request.keyword)
                    .param("sortOrder", request.sortOrder.toString())
                    .param("page", request.page.toString())
                    .param("size", request.size.toString())
            )
                .andExpect(status().isOk)
                .andExpect(content().json(objectMapper.writeValueAsString(baseResponse)))
                .andDo(print())

            verify(exactly = 1) { expenseService.search(request, any()) }
        }

        @Test
        fun `키워드와 날짜 범위로 검색하면 필터링된 결과를 반환한다`() {
            // given
            val request = ExpenseDto.Search.Request(
                activityId = activityId,
                keyword = "식사",
                startDate = baseExpenseDate.withDayOfMonth(1),
                endDate = baseExpenseDate.withDayOfMonth(baseExpenseDate.lengthOfMonth()),
                sortOrder = false,
                page = 0,
                size = 20
            )
            every { expenseService.search(request, any()) } returns baseResponse

            // when & then
            mockMvc.perform(
                get("/api/v1/expense/search")
                    .param("activityId", request.activityId.toString())
                    .param("keyword", request.keyword)
                    .param("startDate", request.startDate.toString())
                    .param("endDate", request.endDate.toString())
                    .param("sortOrder", request.sortOrder.toString())
                    .param("page", request.page.toString())
                    .param("size", request.size.toString())
            )
                .andExpect(status().isOk)
                .andExpect(content().json(objectMapper.writeValueAsString(baseResponse)))
                .andDo(print())

            verify(exactly = 1) { expenseService.search(request, any()) }
        }

        @Test
        fun `활동 ID 없이 요청하면 400을 응답한다`() {
            // when & then
            mockMvc.perform(
                get("/api/v1/expense/search")
                    .param("sortOrder", "false")
                    .param("page", "0")
                    .param("size", "20")
            )
                .andExpect(status().isBadRequest)
                .andDo(print())
        }

        @Test
        fun `시작일이 종료일보다 늦으면 400을 응답한다`() {
            // when & then
            mockMvc.perform(
                get("/api/v1/expense/search")
                    .param("activityId", activityId.toString())
                    .param("startDate", "2024-02-28")
                    .param("endDate", "2024-02-01")
            )
                .andExpect(status().isBadRequest)
                .andDo(print())
        }

        @Test
        fun `미래 날짜로 검색하면 400을 응답한다`() {
            // given
            val futureDate = LocalDate.now().plusDays(1)

            // when & then
            mockMvc.perform(
                get("/api/v1/expense/search")
                    .param("activityId", activityId.toString())
                    .param("startDate", futureDate.toString())
                    .param("endDate", futureDate.toString())
            )
                .andExpect(status().isBadRequest)
                .andDo(print())
        }
    }
}