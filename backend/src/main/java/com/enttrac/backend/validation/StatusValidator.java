package com.enttrac.backend.validation;

import com.enttrac.backend.model.MediaType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class StatusValidator implements ConstraintValidator<ValidStatus, String> {

    private MediaType mediaType;

    @Override
    public void initialize(ValidStatus annotation) {
        this.mediaType = annotation.value();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return true;

        boolean valid = mediaType.getAllowedStatuses().contains(value);

        if (!valid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "Invalid status '" + value + "' for " + mediaType.name()
                            + ". Allowed values: " + mediaType.getAllowedStatuses()
            ).addConstraintViolation();
        }

        return valid;
    }
}
