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
 */
package org.eclipse.uprotocol.transport.datamodel;

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