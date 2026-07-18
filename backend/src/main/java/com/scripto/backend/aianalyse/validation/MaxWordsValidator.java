package com.scripto.backend.aianalyse.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class MaxWordsValidator implements ConstraintValidator<MaxWords, String> {

    private int maxWords;

    @Override
    public void initialize(MaxWords constraintAnnotation) {
        this.maxWords = constraintAnnotation.value();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {

        if (value == null) {
            return true;
        }

        value = value.trim();

        if (value.isEmpty()) {
            return true;
        }

        return value.split("\\s+").length <= maxWords;
    }
}