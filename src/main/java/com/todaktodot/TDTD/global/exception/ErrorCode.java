package com.todaktodot.TDTD.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    VOTE_ALREADY_CLOSED("V1001", "이미 마감된 투표입니다.", HttpStatus.CONFLICT),
    VOTE_HAS_PARTICIPANTS("V1002", "참여자가 있는 투표는 수정할 수 없습니다.", HttpStatus.CONFLICT),
    VOTE_DAILY_LIMIT_EXCEEDED("V1003", "당일 생성할 수 있는 투표 수를 초과했습니다.", HttpStatus.FORBIDDEN),
    VOTE_CREATE_BLOCKED("V1004", "신고 누적으로 인해 투표를 생성할 수 없습니다.", HttpStatus.FORBIDDEN),
    VOTE_HIDDEN_BY_REPORTS("V1005", "신고 누적으로 숨김 처리된 투표입니다.", HttpStatus.FORBIDDEN),
    VOTE_ALREADY_REPORTED("V1006", "이미 신고한 투표입니다.", HttpStatus.CONFLICT),
    VOTE_ALREADY_DELETED("V1007", "이미 삭제된 투표입니다.", HttpStatus.CONFLICT),
    VOTE_NOT_FOUND("V1008", "존재하지 않는 투표입니다.", HttpStatus.NOT_FOUND),

    INTERNAL_SERVER_ERROR("C1001", "서버 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}

