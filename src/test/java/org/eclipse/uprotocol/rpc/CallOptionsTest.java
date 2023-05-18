/*
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

package org.eclipse.uprotocol.rpc;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CallOptionsTest {

    @Test
    @DisplayName("Make sure the equals and hash code works")
    public void testHashCodeEquals() {
        EqualsVerifier.forClass(CallOptions.class).usingGetClass().verify();
    }

    @Test
    @DisplayName("Make sure the toString works")
    public void testToString() {
        final CallOptions callOptions = CallOptions.newBuilder()
                .withTimeout(30)
                .withToken("someToken")
                .build();
        String expected = "CallOptions{mTimeout=30, mToken='someToken'}";
        assertEquals(expected, callOptions.toString());
    }

    @Test
    @DisplayName("Test using the DEFAULT CallOptions")
    public void testCreatingCallOptionsDEFAULT() {
        CallOptions callOptions = CallOptions.DEFAULT;
        assertEquals(CallOptions.TIMEOUT_DEFAULT, callOptions.timeout());
        assertTrue(callOptions.token().isEmpty());
    }

    @Test
    @DisplayName("Test creating CallOptions with only a token")
    public void testCreatingCallOptionsWithAToken() {
        final CallOptions callOptions = CallOptions.newBuilder()
                .withToken("someToken")
                .build();
        assertEquals(CallOptions.TIMEOUT_DEFAULT, callOptions.timeout());
        assertTrue(callOptions.token().isPresent());
        String token = callOptions.token().get();
        assertEquals("someToken", token);
    }

    @Test
    @DisplayName("Test creating CallOptions with a null token")
    public void testCreatingCallOptionsWithANullToken() {
        final CallOptions callOptions = CallOptions.newBuilder()
                .withToken(null)
                .build();
        assertEquals(CallOptions.TIMEOUT_DEFAULT, callOptions.timeout());
        assertTrue(callOptions.token().isEmpty());
    }

    @Test
    @DisplayName("Test creating CallOptions with only an empty string token")
    public void testCreatingCallOptionsWithAnEmptyStringToken() {
        final CallOptions callOptions = CallOptions.newBuilder()
                .withToken("")
                .build();
        assertEquals(CallOptions.TIMEOUT_DEFAULT, callOptions.timeout());
        assertTrue(callOptions.token().isEmpty());
    }

    @Test
    @DisplayName("Test creating CallOptions with only a token with only spaces")
    public void testCreatingCallOptionsWithATokenWithOnlySpaces() {
        final CallOptions callOptions = CallOptions.newBuilder()
                .withToken("   ")
                .build();
        assertEquals(CallOptions.TIMEOUT_DEFAULT, callOptions.timeout());
        assertTrue(callOptions.token().isEmpty());
    }

    @Test
    @DisplayName("Test creating CallOptions with only a timeout")
    public void testCreatingCallOptionsWithATimeout() {
        final CallOptions callOptions = CallOptions.newBuilder()
                .withTimeout(30)
                .build();
        assertEquals(30, callOptions.timeout());
        assertTrue(callOptions.token().isEmpty());
    }

    @Test
    @DisplayName("Test creating CallOptions with a negative value timeout, expect the default timeout")
    public void testCreatingCallOptionsWithANegativeTimeout() {
        final CallOptions callOptions = CallOptions.newBuilder()
                .withTimeout(-3)
                .build();
        assertEquals(CallOptions.TIMEOUT_DEFAULT, callOptions.timeout());
        assertTrue(callOptions.token().isEmpty());
    }

}