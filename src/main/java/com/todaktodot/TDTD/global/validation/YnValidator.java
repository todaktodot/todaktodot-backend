package com.todaktodot.TDTD.global.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class YnValidator implements ConstraintValidator<Yn, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        return "Y".equals(value) || "N".equals(value);
    }
}
