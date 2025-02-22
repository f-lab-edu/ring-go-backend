package com.ringgo.common.exception

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val status: HttpStatus,
    val code: String,
    val message: String
) {
    // Common
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "잘못된 입력값입니다"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C002", "서버 오류가 발생했습니다"),
    MISSING_PARAMETER(HttpStatus.BAD_REQUEST, "C003", "필수 파라미터가 누락되었습니다"),
    INVALID_PARAMETER_VALUE(HttpStatus.BAD_REQUEST, "C004", "파라미터 값이 올바르지 않습니다"),

    // Configuration 관련
    INVALID_BASE_URL(HttpStatus.INTERNAL_SERVER_ERROR, "C005", "base-url 설정이 잘못되었습니다"),

    // 모임 관련 에러
    MEETING_NOT_FOUND(HttpStatus.NOT_FOUND, "M001", "모임을 찾을 수 없습니다"),
    INVALID_MEETING_STATUS(HttpStatus.BAD_REQUEST, "M002", "잘못된 모임 상태입니다"),
    INVALID_STATUS_TRANSITION(HttpStatus.BAD_REQUEST, "M003", "허용되지 않는 상태 변경입니다"),
    NOT_MEETING_CREATOR(HttpStatus.FORBIDDEN, "M004", "모임 생성자만 가능합니다"),
    NOT_MEETING_MEMBER(HttpStatus.FORBIDDEN, "M005", "해당 모임의 멤버가 아닙니다"),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "M006", "해당 모임원을 찾을 수 없습니다"),
    CANNOT_KICK_SELF(HttpStatus.BAD_REQUEST, "M007", "자기 자신을 내보낼 수 없습니다"),

    // 초대 관련 에러
    INVALID_INVITE_LINK(HttpStatus.BAD_REQUEST, "I001", "유효하지 않은 초대 링크입니다"),
    EXPIRED_INVITE_LINK(HttpStatus.GONE, "I002", "만료된 초대 링크입니다"),
    INACTIVE_MEETING(HttpStatus.BAD_REQUEST, "I003", "비활성화된 모임입니다"),
    ALREADY_JOINED_MEMBER(HttpStatus.CONFLICT, "I004", "이미 해당 모임에 가입된 멤버입니다"),
    MEETING_MEMBER_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "I005", "모임 인원이 초과되었습니다"),

    // 활동 관련 에러
    INVALID_ACTIVITY_TYPE(HttpStatus.BAD_REQUEST, "A001", "지원하지 않는 활동 유형입니다"),
    ACTIVITY_NOT_FOUND(HttpStatus.NOT_FOUND, "A002", "활동을 찾을 수 없습니다"),
    DUPLICATE_ACTIVITY_TYPE(HttpStatus.CONFLICT, "A003", "해당 모임에 이미 동일한 유형의 활동이 존재합니다"),
    INACTIVE_ACTIVITY(HttpStatus.BAD_REQUEST, "A004", "비활성화된 활동입니다"),

    // 지출 관련 에러
    EXPENSE_NOT_FOUND(HttpStatus.NOT_FOUND, "E001", "지출 기록을 찾을 수 없습니다"),
    INVALID_EXPENSE_AMOUNT(HttpStatus.BAD_REQUEST, "E002", "올바르지 않은 지출 금액입니다"),
    INVALID_EXPENSE_CATEGORY(HttpStatus.BAD_REQUEST, "E003", "올바르지 않은 지출 카테고리입니다"),
    NOT_EXPENSE_CREATOR(HttpStatus.FORBIDDEN, "E004", "지출 기록 작성자만 가능합니다"),
    INVALID_DATE_RANGE(HttpStatus.BAD_REQUEST, "E005", "종료일이 시작일보다 빠를 수 없습니다"),
    INVALID_FUTURE_DATE(HttpStatus.BAD_REQUEST, "E006", "미래 날짜는 입력할 수 없습니다"),
    INVALID_DATE_FORMAT(HttpStatus.BAD_REQUEST, "E007", "날짜 형식(YYYY-MM-DD)이 올바르지 않거나 존재하지 않는 날짜입니다")
}