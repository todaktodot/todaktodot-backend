package com.todaktodot.TDTD.domain.login.respository.entity;

import java.util.Arrays;

public enum Gender {
    MALE("M"),
    FEMALE("F"),
    UNKNOWN(null);

    private final String code;

    Gender(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static Gender fromCode(String code) {
        if (code == null || code.isBlank()) {
            return UNKNOWN;
        }

        return Arrays.stream(values())
                .filter(gender -> code.equalsIgnoreCase(gender.code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid gender code: " + code));
    }
}
