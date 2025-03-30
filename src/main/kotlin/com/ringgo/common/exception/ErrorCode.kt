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
    INVALID_BASE_URL(HttpStatus.INTERNAL_SERVER_ERROR, "C004", "base-url 설정이 잘못되었습니다"),

    // Auth
    INVALID_PROVIDER(HttpStatus.BAD_REQUEST, "A001", "지원하지 않는 소셜 로그인입니다"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "A002", "유효하지 않은 토큰입니다"),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "A003", "유효하지 않은 리프레시 토큰입니다"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "A004", "사용자를 찾을 수 없습니다"),
    EXTERNAL_API_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "A005", "외부 API 호출 중 오류가 발생했습니다"),
    INACTIVE_USER(HttpStatus.FORBIDDEN, "A006", "비활성화된 계정입니다"),
    AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "A007", "로그인이 필요한 서비스입니다"),
    EMAIL_REQUIRED(HttpStatus.BAD_REQUEST, "A008", "이메일은 필수입니다"),
    NAME_REQUIRED(HttpStatus.BAD_REQUEST, "A009", "이름은 필수입니다"),
    PROVIDER_ID_REQUIRED(HttpStatus.BAD_REQUEST, "A010", "프로바이더 ID는 필수입니다"),

    // JWT 관련 오류 코드
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "A011", "인증 토큰이 만료되었습니다. 다시 로그인해 주세요"),
    TOKEN_INVALID_SIGNATURE(HttpStatus.UNAUTHORIZED, "A012", "유효하지 않은 인증 토큰입니다. 다시 로그인해 주세요"),
    TOKEN_MALFORMED(HttpStatus.UNAUTHORIZED, "A013", "잘못된 형식의 토큰입니다"),
    TOKEN_UNSUPPORTED(HttpStatus.UNAUTHORIZED, "A014", "지원하지 않는 토큰입니다"),
    TOKEN_MISSING(HttpStatus.UNAUTHORIZED, "A015", "인증 토큰이 필요합니다"),
    PROFILE_REQUIRED(HttpStatus.FORBIDDEN, "A016", "프로필 등록이 필요합니다"),
    USER_ID_MISMATCH(HttpStatus.FORBIDDEN, "A017", "요청 사용자 ID가 로그인한 사용자와 일치하지 않습니다"),

    // Meeting
    MEETING_NOT_FOUND(HttpStatus.NOT_FOUND, "M001", "모임을 찾을 수 없습니다"),
    INVALID_MEETING_STATUS(HttpStatus.BAD_REQUEST, "M002", "잘못된 모임 상태입니다"),
    INVALID_STATUS_TRANSITION(HttpStatus.BAD_REQUEST, "M003", "허용되지 않는 상태 변경입니다"),
    NOT_MEETING_CREATOR(HttpStatus.FORBIDDEN, "M004", "모임 생성자만 가능합니다"),
    NOT_MEETING_MEMBER(HttpStatus.FORBIDDEN, "M005", "해당 모임의 멤버가 아닙니다"),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "M006", "해당 모임원을 찾을 수 없습니다"),
    CANNOT_KICK_SELF(HttpStatus.BAD_REQUEST, "M007", "자기 자신을 내보낼 수 없습니다"),

    // Invite
    INVALID_INVITE_LINK(HttpStatus.BAD_REQUEST, "I001", "유효하지 않은 초대 링크입니다"),
    EXPIRED_INVITE_LINK(HttpStatus.GONE, "I002", "만료된 초대 링크입니다"),
    INACTIVE_MEETING(HttpStatus.BAD_REQUEST, "I003", "비활성화된 모임입니다"),
    ALREADY_JOINED_MEMBER(HttpStatus.CONFLICT, "I004", "이미 해당 모임에 가입된 멤버입니다"),
    MEETING_MEMBER_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "I005", "모임 인원이 초과되었습니다"),
}
