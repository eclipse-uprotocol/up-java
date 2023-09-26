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

import static org.junit.jupiter.api.Assertions.*;

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