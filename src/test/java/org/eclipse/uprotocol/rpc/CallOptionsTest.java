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

package org.eclipse.uprotocol.rpc;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.eclipse.uprotocol.v1.CallOptions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CallOptionsTest {

    @Test
    @DisplayName("Make sure the toString works")
    public void testToString() {
        final CallOptions callOptions = CallOptions.newBuilder()
                .setTtl(30)
                .setToken("someToken")
                .build();
        assertEquals(30, callOptions.getTtl());
        assertEquals("someToken", callOptions.getToken());
    }

   
    @Test
    @DisplayName("Test creating CallOptions with only a token")
    public void testCreatingCallOptionsWithAToken() {
        final CallOptions callOptions = CallOptions.newBuilder()
                .setToken("someToken")
                .build();

        assertTrue(callOptions.hasToken());
        String token = callOptions.getToken();
        assertEquals("someToken", token);
    }

    @Test
    @DisplayName("Test creating CallOptions without token")
    public void testCreatingCallOptionsWithoutToken() {
        final CallOptions callOptions = CallOptions.newBuilder()
                .build();
        assertFalse(callOptions.hasToken());
    }

    @Test
    @DisplayName("Test creating CallOptions with only an empty string token")
    public void testCreatingCallOptionsWithAnEmptyStringToken() {
        final CallOptions callOptions = CallOptions.newBuilder()
                .setToken("")
                .build();
            assertTrue(callOptions.getToken().isEmpty());
    }

    @Test
    @DisplayName("Test creating CallOptions with only a token with only spaces")
    public void testCreatingCallOptionsWithATokenWithOnlySpaces() {
        final CallOptions callOptions = CallOptions.newBuilder()
                .setToken("   ")
                .build();
        assertTrue(callOptions.getToken().isBlank());
    }

    @Test
    @DisplayName("Test creating CallOptions with only a timeout")
    public void testCreatingCallOptionsWithATimeout() {
        final CallOptions callOptions = CallOptions.newBuilder()
                .setTtl(30)
                .build();
        assertEquals(30, callOptions.getTtl());
        assertTrue(callOptions.getToken().isEmpty());
    }

    @Test
    @DisplayName("Test creating empty CallOptions ")
    public void testCreatingCallOptionsWithANegativeTimeout() {
        final CallOptions callOptions = CallOptions.newBuilder()
                .build();
        
         assertFalse(callOptions.hasToken());
         assertFalse(callOptions.hasTtl());
    }

}