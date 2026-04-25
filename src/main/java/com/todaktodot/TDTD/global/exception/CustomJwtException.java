package com.todaktodot.TDTD.global.exception;

public class CustomJwtException extends RuntimeException{
    public CustomJwtException(String message) {
        super(message);
    }
}
