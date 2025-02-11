package com.ringgo.common.exception

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val status: HttpStatus,
    val code: String,
    val message: String
) {
    // Common
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "잘못된 입력값입니다"),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C002", "지원하지 않는 메서드입니다"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C003", "서버 오류가 발생했습니다"),

    // Configuration 관련 에러 추가
    INVALID_BASE_URL(HttpStatus.INTERNAL_SERVER_ERROR, "C004", "base-url 설정이 잘못되었습니다"),

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
}