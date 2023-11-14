/*
 * Copyright (c) 2023 General Motors GTO LLC
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * SPDX-FileType: SOURCE
 * SPDX-FileCopyrightText: 2023 General Motors GTO LLC
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
    @DisplayName("Make sure the toString works")
    public void testToString() {
        final UCloudEventAttributes uCloudEventAttributes = new UCloudEventAttributes.UCloudEventAttributesBuilder()
                .withHash("somehash")
                .withPriority(UPriority.UPRIORITY_CS1)
                .withTtl(3)
                .withToken("someOAuthToken")
                .build();
        String expected = "UCloudEventAttributes{hash='somehash', priority=UPRIORITY_CS1, ttl=3, token='someOAuthToken'}";
        assertEquals(expected, uCloudEventAttributes.toString());

    }

    @Test
    @DisplayName("Test creating a valid attributes object")
    public void test_create_valid() {
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
    public void test_Isempty_function() {
        final UCloudEventAttributes uCloudEventAttributes = UCloudEventAttributes.empty();
        assertTrue(uCloudEventAttributes.isEmpty());
        assertTrue(uCloudEventAttributes.hash().isEmpty());
        assertTrue(uCloudEventAttributes.priority().isEmpty());
        assertTrue(uCloudEventAttributes.token().isEmpty());
        assertTrue(uCloudEventAttributes.ttl().isEmpty());
    }

    @Test
    @DisplayName("Test the isEmpty when built with blank strings function")
    public void test_Isempty_function_when_built_with_blank_strings() {
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
    public void test_Isempty_function_permutations() {
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