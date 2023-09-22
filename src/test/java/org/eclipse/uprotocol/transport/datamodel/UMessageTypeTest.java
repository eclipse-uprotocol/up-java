package org.eclipse.uprotocol.transport.datamodel;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.uprotocol.transport.datamodel.UMessageType;

class UMessageTypeTest {

    @Test
    @DisplayName("Test finding UMessageType from a numeric value")
    public void test_find_UMessageType_from_number() {
        assertTrue(UMessageType.from(0).isPresent());
        assertEquals(UMessageType.PUBLISH, UMessageType.from(0).get());

        assertTrue(UMessageType.from(1).isPresent());
        assertEquals(UMessageType.REQUEST, UMessageType.from(1).get());

        assertTrue(UMessageType.from(2).isPresent());
        assertEquals(UMessageType.RESPONSE, UMessageType.from(2).get());
        
    }

    @Test
    @DisplayName("Test finding UMessageType from a numeric value that does not exist")
    public void test_find_UMessageType_from_number_that_does_not_exist() {
        assertTrue(UMessageType.from(-42).isEmpty());
    }

    @Test
    @DisplayName("Test finding UMessageType from a string value")
    public void test_find_UMessageType_from_string() {
        assertTrue(UMessageType.from("pub.v1").isPresent());
        assertEquals(UMessageType.PUBLISH, UMessageType.from("pub.v1").get());

        assertTrue(UMessageType.from("req.v1").isPresent());
        assertEquals(UMessageType.REQUEST, UMessageType.from("req.v1").get());

        assertTrue(UMessageType.from("res.v1").isPresent());
        assertEquals(UMessageType.RESPONSE, UMessageType.from("res.v1").get());

    }

    @Test
    @DisplayName("Test finding UMessageType from a numeric string that does not exist")
    public void test_find_UMessageType_from_string_that_does_not_exist() {
        assertTrue(UMessageType.from("BOOM").isEmpty());
    }

}