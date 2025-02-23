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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@WebMvcTest(ExpenseController::class)
@AutoConfigureMockMvc(addFilters = false)
class ExpenseControllerTest {

    companion object {
        private val FIXED_DATE = LocalDate.of(2024, 2, 14)
        private val FIXED_DATETIME = FIXED_DATE.atStartOfDay(ZoneOffset.UTC).toInstant()
        private const val DEFAULT_EXPENSE_ID = 1L
        private const val DEFAULT_ACTIVITY_ID = 1L
        private val DEFAULT_AMOUNT = BigDecimal("15000.00")
        private const val DEFAULT_EXPENSE_NAME = "점심 식사"
        private const val DEFAULT_DESCRIPTION = "팀원들과 점심 식사"
        private const val API_PATH = "/api/v1/expense"
    }

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var expenseService: ExpenseService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private val testUser = TestUser.create()

    @BeforeEach
    fun setUpMockAuth() {
        SecurityContextHolder.getContext().authentication =
            UsernamePasswordAuthenticationToken(testUser, null, emptyList())
    }

    @AfterEach
    fun cleanup() {
        SecurityContextHolder.clearContext()
    }

    private fun createExpenseItem(
        id: Long = DEFAULT_EXPENSE_ID,
        name: String = "점심 식사",
        amount: BigDecimal = DEFAULT_AMOUNT,
        category: ExpenseCategory = ExpenseCategory.FOOD,
        description: String = "팀원들과 점심 식사",
        createdAt: Instant = FIXED_DATETIME
    ) = ExpenseDto.ExpenseItem(
        id = id,
        name = name,
        amount = amount,
        category = category,
        description = description,
        createdAt = createdAt
    )

    private fun createSearchRequest(
        activityId: Long = DEFAULT_ACTIVITY_ID,
        keyword: String? = null,
        startDate: LocalDate? = null,
        endDate: LocalDate? = null,
        sortOrder: Boolean = false,
        page: Int = 0,
        size: Int = 20
    ) = ExpenseDto.Search.Request(
        activityId = activityId,
        keyword = keyword,
        startDate = startDate,
        endDate = endDate,
        sortOrder = sortOrder,
        page = page,
        size = size
    )

    private fun createSearchResponse(
        expenses: List<ExpenseDto.ExpenseItem> = listOf(createExpenseItem()),
        date: LocalDate = FIXED_DATE,
        totalAmount: BigDecimal = DEFAULT_AMOUNT
    ) = ExpenseDto.Search.Response(
        dailyExpenses = listOf(
            ExpenseDto.DailyExpense(
                date = date.atStartOfDay(ZoneOffset.UTC).toInstant(),
                userExpenses = listOf(
                    ExpenseDto.UserExpenseCommon(
                        userId = testUser.id,
                        userName = testUser.name,
                        expenses = expenses,
                        totalAmount = totalAmount
                    )
                )
            )
        ),
        metadata = ExpenseDto.Search.SearchMetadata(
            totalElements = expenses.size.toLong(),
            totalPages = 1,
            currentPage = 0,
            pageSize = 20,
            totalAmount = totalAmount
        )
    )

    private fun createExpenseRequest(
        activityId: Long = DEFAULT_ACTIVITY_ID,
        name: String = DEFAULT_EXPENSE_NAME,
        amount: BigDecimal = DEFAULT_AMOUNT,
        category: ExpenseCategory = ExpenseCategory.FOOD,
        description: String = DEFAULT_DESCRIPTION,
        expenseDate: LocalDate = FIXED_DATE
    ) = ExpenseDto.Create.Request(
        activityId = activityId,
        name = name,
        amount = amount,
        category = category,
        description = description,
        expenseDate = expenseDate
    )

    @Nested
    @DisplayName("지출 생성 API")
    inner class CreateExpense {
        @Test
        fun `지출 생성 성공시 201을 응답한다`() {
            // given
            val request = createExpenseRequest()
            val expectedResponse = ExpenseDto.Create.Response(
                id = DEFAULT_EXPENSE_ID,
                createdAt = FIXED_DATETIME
            )
            every { expenseService.create(request, any()) } returns expectedResponse

            // when
            val performRequest = mockMvc.perform(
                post(API_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )

            // then
            performRequest
                .andExpect(status().isCreated)
                .andExpect(content().json(objectMapper.writeValueAsString(expectedResponse)))
                .andDo(print())

            verify(exactly = 1) { expenseService.create(request, any()) }
        }

        @Test
        fun `유효하지 않은 요청시 400을 응답한다`() {
            // given
            val invalidRequest = createExpenseRequest(name = "")

            // when
            val performRequest = mockMvc.perform(
                post(API_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest))
            )

            // then
            performRequest
                .andExpect(status().isBadRequest)
                .andDo(print())
        }

        @Test
        fun `활동을 찾을 수 없으면 404를 응답한다`() {
            // given
            val request = createExpenseRequest()
            every { expenseService.create(request, any()) } throws
                    ApplicationException(ErrorCode.ACTIVITY_NOT_FOUND)

            // when
            val performRequest = mockMvc.perform(
                post(API_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )

            // then
            performRequest
                .andExpect(status().isNotFound)
                .andDo(print())

            verify(exactly = 1) { expenseService.create(request, any()) }
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
            expenseDate = LocalDate.of(2025, 2, 14)
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
        private fun createListRequest(
            activityId: Long = DEFAULT_ACTIVITY_ID,
            sortOrder: Boolean = false
        ) = ExpenseDto.Get.Request(
            activityId = activityId,
            sortOrder = sortOrder
        )

        private fun createListResponse(
            expenseItems: List<ExpenseDto.ExpenseItem> = listOf(createExpenseItem()),
            totalAmount: BigDecimal = DEFAULT_AMOUNT
        ) = ExpenseDto.Get.Response(
            dailyExpenses = listOf(
                ExpenseDto.DailyExpense(
                    date = FIXED_DATETIME,
                    userExpenses = listOf(
                        ExpenseDto.UserExpenseCommon(
                            userId = testUser.id,
                            userName = testUser.name,
                            expenses = expenseItems,
                            totalAmount = totalAmount
                        )
                    )
                )
            )
        )

        @Test
        fun `지출 목록 조회 성공시 200을 응답한다`() {
            // given
            val request = createListRequest()
            val response = createListResponse()
            every { expenseService.list(request, any()) } returns response

            // when
            val performRequest = mockMvc.perform(
                get(API_PATH)
                    .param("activityId", request.activityId.toString())
                    .param("sortOrder", request.sortOrder.toString())
            )

            // then
            performRequest
                .andExpect(status().isOk)
                .andExpect(content().json(objectMapper.writeValueAsString(response)))
                .andDo(print())

            verify(exactly = 1) { expenseService.list(request, any()) }
        }

        @Test
        fun `활동을 찾을 수 없으면 404를 응답한다`() {
            // given
            val request = createListRequest()
            every { expenseService.list(request, any()) } throws
                    ApplicationException(ErrorCode.ACTIVITY_NOT_FOUND)

            // when
            val performRequest = mockMvc.perform(
                get(API_PATH)
                    .param("activityId", request.activityId.toString())
            )

            // then
            performRequest
                .andExpect(status().isNotFound)
                .andDo(print())

            verify(exactly = 1) { expenseService.list(request, any()) }
        }

        @Test
        fun `모임 멤버가 아니면 403을 응답한다`() {
            // given
            val request = createListRequest()
            every { expenseService.list(request, any()) } throws
                    ApplicationException(ErrorCode.NOT_MEETING_MEMBER)

            // when
            val performRequest = mockMvc.perform(
                get(API_PATH)
                    .param("activityId", request.activityId.toString())
            )

            // then
            performRequest
                .andExpect(status().isForbidden)
                .andDo(print())

            verify(exactly = 1) { expenseService.list(request, any()) }
        }

        @Test
        fun `활동 ID 없이 요청하면 400을 응답한다`() {
            // when
            val performRequest = mockMvc.perform(get(API_PATH))

            // then
            performRequest
                .andExpect(status().isBadRequest)
                .andDo(print())
        }
    }

    @Nested
    @DisplayName("지출 검색 API")
    inner class SearchExpense {
        private fun createSearchRequestParams(
            request: ExpenseDto.Search.Request
        ): Array<MockHttpServletRequestBuilder.() -> MockHttpServletRequestBuilder> = arrayOf(
            { param("activityId", request.activityId.toString()) },
            { param("keyword", request.keyword) },
            { param("startDate", request.startDate?.toString()) },
            { param("endDate", request.endDate?.toString()) },
            { param("sortOrder", request.sortOrder.toString()) },
            { param("page", request.page.toString()) },
            { param("size", request.size.toString()) }
        )

        @Test
        fun `키워드와 날짜 범위로 검색하면 필터링된 결과를 반환한다`() {
            // given
            val request = createSearchRequest(
                activityId = DEFAULT_ACTIVITY_ID,
                keyword = "식사",
                startDate = FIXED_DATE.minusDays(13),
                endDate = FIXED_DATE
            )
            val expenseItem = createExpenseItem()
            val response = createSearchResponse(
                expenses = listOf(expenseItem),
                date = FIXED_DATE,
                totalAmount = DEFAULT_AMOUNT
            )
            every { expenseService.search(request, any()) } returns response

            // when
            val performRequest = mockMvc.perform(
                get("$API_PATH/search").apply {
                    createSearchRequestParams(request).forEach { it() }
                }
            )

            // then
            performRequest
                .andExpect(status().isOk)
                .andExpect(content().json(objectMapper.writeValueAsString(response)))
                .andDo(print())

            verify(exactly = 1) { expenseService.search(request, any()) }
        }

        @Test
        fun `활동 ID 없이 요청하면 400을 응답한다`() {
            // when
            val performRequest = mockMvc.perform(
                get("$API_PATH/search")
                    .param("sortOrder", "false")
                    .param("page", "0")
                    .param("size", "20")
            )

            // then
            performRequest
                .andExpect(status().isBadRequest)
                .andDo(print())
        }

        @Test
        fun `시작일이 종료일보다 늦으면 400을 응답한다`() {
            // given
            val request = createSearchRequest(
                activityId = DEFAULT_ACTIVITY_ID,
                startDate = FIXED_DATE,
                endDate = FIXED_DATE.minusDays(1)
            )
            every { expenseService.search(request, any()) } throws
                    ApplicationException(ErrorCode.INVALID_DATE_RANGE)

            // when
            val performRequest = mockMvc.perform(
                get("$API_PATH/search").apply {
                    createSearchRequestParams(request).forEach { it() }
                }
            )

            // then
            performRequest
                .andExpect(status().isBadRequest)
                .andDo(print())

            verify(exactly = 1) { expenseService.search(request, any()) }
        }

        @Test
        fun `미래 날짜로 검색하면 400을 응답한다`() {
            // given
            val request = createSearchRequest(
                activityId = DEFAULT_ACTIVITY_ID,
                startDate = FIXED_DATE.plusDays(1),
                endDate = FIXED_DATE.plusDays(1)
            )
            every { expenseService.search(request, any()) } throws
                    ApplicationException(ErrorCode.INVALID_FUTURE_DATE)

            // when
            val performRequest = mockMvc.perform(
                get("$API_PATH/search").apply {
                    createSearchRequestParams(request).forEach { it() }
                }
            )

            // then
            performRequest
                .andExpect(status().isBadRequest)
                .andDo(print())

            verify(exactly = 1) { expenseService.search(request, any()) }
        }
    }
}