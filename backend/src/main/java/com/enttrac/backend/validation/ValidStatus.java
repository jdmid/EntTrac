package com.enttrac.backend.validation;

import com.enttrac.backend.model.MediaType;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = StatusValidator.class)
@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidStatus {

    MediaType value();

    String message() default "Invalid status for media type";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}