package com.scripto.backend.aianalyse.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = MaxWordsValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface MaxWords {

    String message() default "O texto deve possuir no máximo {value} palavras.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    int value();
}