package com.todaktodot.TDTD.global.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UserAnswerValidator.class)
public @interface UserAnswer {
    String message() default "답변 형식이 올바르지 않습니다";
    int max() default 500;
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
