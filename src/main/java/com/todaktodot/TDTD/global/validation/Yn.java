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
@Constraint(validatedBy = YnValidator.class)
public @interface Yn {
    String message() default "Y 또는 N만 입력할 수 있습니다";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
