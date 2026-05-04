package com.todaktodot.TDTD.global.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UserAnswerValidator implements ConstraintValidator<UserAnswer, String> {

    private int max;

    @Override
    public void initialize(UserAnswer constraintAnnotation) {
        this.max = constraintAnnotation.max();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            addViolation(context, "답변 내용은 필수입니다");
            return false;
        }

        if (value.length() > max) {
            addViolation(context, "답변은 " + max + "자 이하로 입력해주세요");
            return false;
        }

        if (TextValidationUtils.containsDisallowedControlCharacter(value)) {
            addViolation(context, "답변에는 제어 문자를 입력할 수 없습니다");
            return false;
        }

        if (TextValidationUtils.containsHtmlTagLikeText(value)) {
            addViolation(context, "답변에는 HTML 태그를 입력할 수 없습니다");
            return false;
        }

        return true;
    }

    private void addViolation(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    }
}
