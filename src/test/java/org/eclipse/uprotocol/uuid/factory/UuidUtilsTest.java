/*
 * Copyright (c) 2024 General Motors GTO LLC
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
 * SPDX-FileCopyrightText: 2024 General Motors GTO LLC
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.uprotocol.uuid.factory;

import org.eclipse.uprotocol.transport.builder.UAttributesBuilder;
import org.eclipse.uprotocol.v1.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.eclipse.uprotocol.v1.UPriority.UPRIORITY_CS0;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;

public class UuidUtilsTest {
    private static final int DELTA = 30;
    private static final int DELAY_MS = 100;
    private static final long DELAY_LONG_MS = 100000;
    private static final int TTL = 10000;


    @Test
    public void testGetElapsedTime() throws InterruptedException {
        final UUID id = createId();
        Thread.sleep(DELAY_MS);
        assertEquals(DELAY_MS, UuidUtils.getElapsedTime(id).orElseThrow(), DELTA);
    }

    private UUID createId() {
        return UuidFactory.Factories.UPROTOCOL.factory().create();
    }

    private UUri buildSourceForTest() {
        UUri uuri = UUri.newBuilder().setEntity(UEntity.newBuilder().setName("body.access"))
                .setResource(UResource.newBuilder().setName("door").setInstance("front_left").setMessage("Door"))
                .build();

        return uuri;
    }

    @Test
    public void testGetElapsedTimeCreationTimeUnknown() {
        assertFalse(UuidUtils.getElapsedTime(UUID.getDefaultInstance()).isPresent());
    }

    @Test
    public void testGetRemainingTime() throws InterruptedException {
        final UUID id = createId();
        assertEquals(TTL, UuidUtils.getRemainingTime(id, TTL).orElseThrow(), DELTA);
        Thread.sleep(DELAY_MS);
        assertEquals(TTL - DELAY_MS, UuidUtils.getRemainingTime(id, TTL).orElseThrow(), DELTA);
    }

    @Test
    public void testGetRemainingTimeNoTtl() {
        final UUID id = createId();
        assertFalse(UuidUtils.getRemainingTime(id, 0).isPresent());
        assertFalse(UuidUtils.getRemainingTime(id, -1).isPresent());
    }

    @Test
    public void testGetRemainingTimeNullUUID() {
        assertFalse(UuidUtils.getRemainingTime(null, 0).isPresent());
    }

    @Test
    public void testGetRemainingTimeExpired() throws InterruptedException {
        final UUID id = createId();
        Thread.sleep(DELAY_MS);
        assertFalse(UuidUtils.getRemainingTime(id, DELAY_MS - DELTA).isPresent());
    }

    @Test
    public void testGetRemainingTimeAttributes() throws InterruptedException {
        final UAttributes attributes = UAttributesBuilder.publish(buildSourceForTest(), UPRIORITY_CS0).withTtl(TTL)
                .build();
        assertEquals(TTL, UuidUtils.getRemainingTime(attributes).orElseThrow(), DELTA);
        Thread.sleep(DELAY_MS);
        assertEquals(TTL - DELAY_MS, UuidUtils.getRemainingTime(attributes).orElseThrow(), DELTA);
    }

    @Test
    public void testGetRemainingTimeAttributesNoTtl() {
        final UAttributes attributes = UAttributesBuilder.publish(buildSourceForTest(), UPRIORITY_CS0).build();
        assertFalse(UuidUtils.getRemainingTime(attributes).isPresent());
    }

    @Test
    public void testGetRemainingTimeAttributesExpired() throws InterruptedException {
        final UAttributes attributes = UAttributesBuilder.publish(buildSourceForTest(), UPRIORITY_CS0)
                .withTtl(DELAY_MS - DELTA).build();
        Thread.sleep(DELAY_MS);
        assertFalse(UuidUtils.getRemainingTime(attributes).isPresent());
    }

    @Test
    public void testIsExpired() throws InterruptedException {
        final UUID id = createId();
        assertFalse(UuidUtils.isExpired(id, DELAY_MS - DELTA));
        Thread.sleep(DELAY_MS);
        assertTrue(UuidUtils.isExpired(id, DELAY_MS - DELTA));
    }

    @Test
    public void testIsExpiredNoTtl() {
        final UUID id = createId();
        assertFalse(UuidUtils.isExpired(id, 0));
        assertFalse(UuidUtils.isExpired(id, -1));
    }

    @Test
    public void testIsExpiredAttributes() throws InterruptedException {
        final UAttributes attributes = UAttributesBuilder.publish(buildSourceForTest(), UPRIORITY_CS0)
                .withTtl(DELAY_MS - DELTA).build();
        assertFalse(UuidUtils.isExpired(attributes));
        Thread.sleep(DELAY_MS);
        assertTrue(UuidUtils.isExpired(attributes));
    }

    @Test
    public void testIsExpiredAttributesNoTtl() {
        final UAttributes attributes = UAttributesBuilder.publish(buildSourceForTest(), UPRIORITY_CS0).build();
        assertFalse(UuidUtils.isExpired(attributes));
    }

    @Test
    @DisplayName("Test getElapseTime() when passed invalid UUID")
    public void testGetElapsedTimeInvalidUUID() {
        assertFalse(UuidUtils.getElapsedTime(null).isPresent());
    }

    @Test
    @DisplayName("Test getElapseTime() when UUID time is in the future")
    public void testGetElapsedTimePast() throws InterruptedException {
        final Instant now = Instant.now().plusMillis(DELAY_MS);
        final UUID id = UuidFactory.Factories.UPROTOCOL.factory().create(now);
        assertTrue(UuidUtils.getElapsedTime(id).isEmpty());
    }

}
