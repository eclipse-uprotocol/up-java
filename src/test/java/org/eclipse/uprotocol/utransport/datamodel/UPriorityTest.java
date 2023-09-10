package org.eclipse.uprotocol.utransport.datamodel;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UPriorityTest {

    @Test
    @DisplayName("Test finding UPriority from a numeric value")
    public void test_find_upriority_from_number() {
        assertTrue(UPriority.from(0).isPresent());
        assertEquals(UPriority.LOW, UPriority.from(0).get());

        assertTrue(UPriority.from(1).isPresent());
        assertEquals(UPriority.STANDARD, UPriority.from(1).get());

        assertTrue(UPriority.from(2).isPresent());
        assertEquals(UPriority.OPERATIONS, UPriority.from(2).get());

        assertTrue(UPriority.from(3).isPresent());
        assertEquals(UPriority.MULTIMEDIA_STREAMING, UPriority.from(3).get());

        assertTrue(UPriority.from(4).isPresent());
        assertEquals(UPriority.REALTIME_INTERACTIVE, UPriority.from(4).get());

        assertTrue(UPriority.from(5).isPresent());
        assertEquals(UPriority.SIGNALING, UPriority.from(5).get());

        assertTrue(UPriority.from(6).isPresent());
        assertEquals(UPriority.NETWORK_CONTROL, UPriority.from(6).get());
    }

    @Test
    @DisplayName("Test finding UPriority from a numeric value that does not exist")
    public void test_find_upriority_from_number_that_does_not_exist() {
        assertTrue(UPriority.from(-42).isEmpty());
    }

    @Test
    @DisplayName("Test finding UPriority from a string value")
    public void test_find_upriority_from_string() {
        assertTrue(UPriority.from("CS0").isPresent());
        assertEquals(UPriority.LOW, UPriority.from("CS0").get());

        assertTrue(UPriority.from("CS1").isPresent());
        assertEquals(UPriority.STANDARD, UPriority.from("CS1").get());

        assertTrue(UPriority.from("CS2").isPresent());
        assertEquals(UPriority.OPERATIONS, UPriority.from("CS2").get());

        assertTrue(UPriority.from("CS3").isPresent());
        assertEquals(UPriority.MULTIMEDIA_STREAMING, UPriority.from("CS3").get());

        assertTrue(UPriority.from("CS4").isPresent());
        assertEquals(UPriority.REALTIME_INTERACTIVE, UPriority.from("CS4").get());

        assertTrue(UPriority.from("CS5").isPresent());
        assertEquals(UPriority.SIGNALING, UPriority.from("CS5").get());

        assertTrue(UPriority.from("CS6").isPresent());
        assertEquals(UPriority.NETWORK_CONTROL, UPriority.from("CS6").get());
    }

    @Test
    @DisplayName("Test finding UPriority from a numeric string that does not exist")
    public void test_find_upriority_from_string_that_does_not_exist() {
        assertTrue(UPriority.from("BOOM").isEmpty());
    }

}