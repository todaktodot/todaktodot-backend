package com.todaktodot.TDTD.global.exception;

import lombok.Getter;

@Getter
public abstract class TdtdException extends RuntimeException{
    private final ErrorCode errorCode;

    public TdtdException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
