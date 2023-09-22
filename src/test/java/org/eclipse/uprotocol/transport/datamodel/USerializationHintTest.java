package org.eclipse.uprotocol.transport.datamodel;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.uprotocol.transport.datamodel.USerializationHint;

class USerializationHintTest {

    @Test
    @DisplayName("Test finding USerializationHint from a numeric value")
    public void test_find_USerializationHint_from_number() {
        assertTrue(USerializationHint.from(0).isPresent());
        assertEquals(USerializationHint.UNKNOWN, USerializationHint.from(0).get());

        assertTrue(USerializationHint.from(1).isPresent());
        assertEquals(USerializationHint.PROTOBUF, USerializationHint.from(1).get());

        assertTrue(USerializationHint.from(2).isPresent());
        assertEquals(USerializationHint.JSON, USerializationHint.from(2).get());

        assertTrue(USerializationHint.from(3).isPresent());
        assertEquals(USerializationHint.SOMEIP, USerializationHint.from(3).get());

        assertTrue(USerializationHint.from(4).isPresent());
        assertEquals(USerializationHint.RAW, USerializationHint.from(4).get());

    }

    @Test
    @DisplayName("Test finding USerializationHint from a numeric value that does not exist")
    public void test_find_USerializationHint_from_number_that_does_not_exist() {
        assertTrue(USerializationHint.from(-42).isEmpty());
    }

    @Test
    @DisplayName("Test finding USerializationHint from a string value")
    public void test_find_USerializationHint_from_string() {
        assertTrue(USerializationHint.from("").isPresent());
        assertEquals(USerializationHint.UNKNOWN, USerializationHint.from("").get());

        assertTrue(USerializationHint.from("application/x-protobuf").isPresent());
        assertEquals(USerializationHint.PROTOBUF, USerializationHint.from("application/x-protobuf").get());

        assertTrue(USerializationHint.from("application/json").isPresent());
        assertEquals(USerializationHint.JSON, USerializationHint.from("application/json").get());

        assertTrue(USerializationHint.from("application/x-someip").isPresent());
        assertEquals(USerializationHint.SOMEIP, USerializationHint.from("application/x-someip").get());

        assertTrue(USerializationHint.from("application/octet-stream").isPresent());
        assertEquals(USerializationHint.RAW, USerializationHint.from("application/octet-stream").get());

    }

    @Test
    @DisplayName("Test finding USerializationHint from a numeric string that does not exist")
    public void test_find_USerializationHint_from_string_that_does_not_exist() {
        assertTrue(USerializationHint.from("BOOM").isEmpty());
    }

}