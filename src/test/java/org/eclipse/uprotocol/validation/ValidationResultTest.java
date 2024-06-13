package org.eclipse.uprotocol.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.eclipse.uprotocol.v1.UCode;

public class ValidationResultTest {
    @Test
    @DisplayName("Test creating a successful ValidationResult")
    void testCreateSuccess() {
        ValidationResult result = ValidationResult.success();
        assertTrue(result.isSuccess());
        assertFalse(result.isFailure());
        assertEquals("", result.getMessage());
        assertEquals(result.toStatus().getCode(), UCode.OK);
        assertEquals(result.toString(), "ValidationResult.Success()");
    }

    @Test
    @DisplayName("Test creating a failed ValidationResult")
    void testCreateFailure() {
        ValidationResult result = ValidationResult.failure("Failed");
        assertFalse(result.isSuccess());
        assertTrue(result.isFailure());
        assertEquals("Failed", result.getMessage());
        assertEquals(result.toStatus().getCode(), UCode.INVALID_ARGUMENT);
        assertEquals(result.toString(), "ValidationResult.Failure(message='Failed')");
    }

}
