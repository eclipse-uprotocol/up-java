/**
 * SPDX-FileCopyrightText: 2024 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.eclipse.uprotocol.cloudevent.datamodel;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.eclipse.uprotocol.v1.UPriority;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UCloudEventAttributesTest {

    @Test
    @DisplayName("Make sure the equals and hash code works")
    public void testHashCodeEquals() {
        EqualsVerifier.forClass(UCloudEventAttributes.class).usingGetClass().verify();
    }

    @Test
    @DisplayName("Make sure the default toString works")
    public void testToString() {
        final UCloudEventAttributes uCloudEventAttributes = new UCloudEventAttributes.UCloudEventAttributesBuilder()
                .withHash("somehash")
                .withPriority(UPriority.UPRIORITY_CS1)
                .withTtl(3)
                .withToken("someOAuthToken")
                .build();
        String expected = """
                UCloudEventAttributes{hash='somehash', priority=UPRIORITY_CS1, ttl=3, token='someOAuthToken'}\
                """;
        assertEquals(expected, uCloudEventAttributes.toString());
    }

    @Test
    @DisplayName("Make sure the toString works when all properties are filled")
    public void testToStringComplete() {
        final UCloudEventAttributes uCloudEventAttributes = new UCloudEventAttributes.UCloudEventAttributesBuilder()
                .withHash("somehash")
                .withPriority(UPriority.UPRIORITY_CS1)
                .withTtl(3)
                .withToken("someOAuthToken")
                .withTraceparent("darthvader")
                .build();
        String expected = """
                UCloudEventAttributes{hash='somehash', priority=UPRIORITY_CS1, ttl=3, \
                token='someOAuthToken', traceparent='darthvader'}\
                """;
        assertEquals(expected, uCloudEventAttributes.toString());
    }
    
    @Test
    @DisplayName("Test creating a valid attributes but traceparent is blank")
    public void testCreateValidWithBlankTraceparent() {
        final UCloudEventAttributes uCloudEventAttributes = new UCloudEventAttributes.UCloudEventAttributesBuilder()
                .withHash("somehash")
                .withPriority(UPriority.UPRIORITY_CS6)
                .withTtl(3)
                .withToken("someOAuthToken")
                .withTraceparent("  ")
                .build();
        assertTrue(uCloudEventAttributes.hash().isPresent());
        assertEquals("somehash", uCloudEventAttributes.hash().get());
        assertFalse(uCloudEventAttributes.traceparent().isPresent());
    }

    @Test
    @DisplayName("Test creating a empty attributes with only traceparent")
    public void testCreateEmptyWithOnlyTraceparent() {
        final UCloudEventAttributes uCloudEventAttributes = new UCloudEventAttributes.UCloudEventAttributesBuilder()
                .withTraceparent("someTraceParent")
                .build();
        assertFalse(uCloudEventAttributes.hash().isPresent());
        assertFalse(uCloudEventAttributes.priority().isPresent());
        assertFalse(uCloudEventAttributes.token().isPresent());
        assertFalse(uCloudEventAttributes.ttl().isPresent());
        assertTrue(uCloudEventAttributes.traceparent().isPresent());
        assertFalse(uCloudEventAttributes.isEmpty());
        assertEquals("someTraceParent", uCloudEventAttributes.traceparent().get());
    }
            
    @Test
    @DisplayName("Test creating a valid attributes object")
    public void testCreateValid() {
        final UCloudEventAttributes uCloudEventAttributes = new UCloudEventAttributes.UCloudEventAttributesBuilder()
                .withHash("somehash")
                .withPriority(UPriority.UPRIORITY_CS6)
                .withTtl(3)
                .withToken("someOAuthToken")
                .build();
        assertTrue(uCloudEventAttributes.hash().isPresent());
        assertEquals("somehash", uCloudEventAttributes.hash().get());
        assertTrue(uCloudEventAttributes.priority().isPresent());
        assertEquals(UPriority.UPRIORITY_CS6, uCloudEventAttributes.priority().get());
        assertTrue(uCloudEventAttributes.ttl().isPresent());
        assertEquals(3, uCloudEventAttributes.ttl().get());
        assertTrue(uCloudEventAttributes.token().isPresent());
        assertEquals("someOAuthToken", uCloudEventAttributes.token().get());
    }

    @Test
    @DisplayName("Test the isEmpty function")
    public void testIsEmptyFunction() {
        final UCloudEventAttributes uCloudEventAttributes = UCloudEventAttributes.empty();
        assertTrue(uCloudEventAttributes.isEmpty());
        assertTrue(uCloudEventAttributes.hash().isEmpty());
        assertTrue(uCloudEventAttributes.priority().isEmpty());
        assertTrue(uCloudEventAttributes.token().isEmpty());
        assertTrue(uCloudEventAttributes.ttl().isEmpty());
    }

    @Test
    @DisplayName("Test the isEmpty when built with blank strings function")
    public void testIsEmptyFunctionWhenBuiltWithBlankStrings() {
        final UCloudEventAttributes uCloudEventAttributes = new UCloudEventAttributes.UCloudEventAttributesBuilder()
                .withHash("  ")
                .withToken("  ")
                .build();
        assertTrue(uCloudEventAttributes.isEmpty());
        assertTrue(uCloudEventAttributes.hash().isEmpty());
        assertTrue(uCloudEventAttributes.priority().isEmpty());
        assertTrue(uCloudEventAttributes.token().isEmpty());
        assertTrue(uCloudEventAttributes.ttl().isEmpty());
    }

    @Test
    @DisplayName("Test the isEmpty permutations")
    public void testIsEmptyFunctionPermutations() {
        final UCloudEventAttributes uCloudEventAttributes = new UCloudEventAttributes.UCloudEventAttributesBuilder()
                .withHash("  ")
                .withToken("  ")
                .build();
        assertTrue(uCloudEventAttributes.isEmpty());

        final UCloudEventAttributes uCloudEventAttributes2 = new UCloudEventAttributes.UCloudEventAttributesBuilder()
                .withHash("someHash")
                .withToken("  ")
                .build();
        assertFalse(uCloudEventAttributes2.isEmpty());

        final UCloudEventAttributes uCloudEventAttributes3 = new UCloudEventAttributes.UCloudEventAttributesBuilder()
                .withHash(" ")
                .withToken("SomeToken")
                .build();
        assertFalse(uCloudEventAttributes3.isEmpty());

        final UCloudEventAttributes uCloudEventAttributes4 = new UCloudEventAttributes.UCloudEventAttributesBuilder()
                .withPriority(UPriority.UPRIORITY_CS0)
                .build();
        assertFalse(uCloudEventAttributes4.isEmpty());

        final UCloudEventAttributes uCloudEventAttributes5 = new UCloudEventAttributes.UCloudEventAttributesBuilder()
                .withTtl(8)
                .build();
        assertFalse(uCloudEventAttributes5.isEmpty());
    }
}
