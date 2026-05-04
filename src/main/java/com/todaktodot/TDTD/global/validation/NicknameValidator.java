package com.todaktodot.TDTD.global.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NicknameValidator implements ConstraintValidator<Nickname, String> {

    private static final int MIN_LENGTH = 1;
    private static final int MAX_LENGTH = 20;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            addViolation(context, "닉네임을 입력해주세요");
            return false;
        }

        int length = value.length();
        if (length < MIN_LENGTH || length > MAX_LENGTH) {
            addViolation(context, "닉네임은 1자 이상 20자 이하로 입력해주세요");
            return false;
        }

        if (TextValidationUtils.containsDisallowedControlCharacter(value)) {
            addViolation(context, "닉네임에는 제어 문자를 입력할 수 없습니다");
            return false;
        }

        if (TextValidationUtils.containsHtmlTagLikeText(value)) {
            addViolation(context, "닉네임에는 HTML 태그를 입력할 수 없습니다");
            return false;
        }

        return true;
    }

    private void addViolation(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    }
}
