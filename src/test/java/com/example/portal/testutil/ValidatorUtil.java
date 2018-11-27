package com.example.portal.testutil;

import org.junit.Assert;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;

import static org.junit.Assert.assertTrue;

public final class ValidatorUtil {

    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    private ValidatorUtil() {
    }

    public static Validator getValidator() {
        return VALIDATOR;
    }

    public static <E> void assertError(E entity) {
        Set<ConstraintViolation<E>> constraintViolations = getValidator().validate(entity);
        assertTrue(constraintViolations.size() > 0);
    }
}