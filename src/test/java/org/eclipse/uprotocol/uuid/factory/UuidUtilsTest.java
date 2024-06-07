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
package org.eclipse.uprotocol.uuid.factory;

import org.eclipse.uprotocol.v1.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;

public class UuidUtilsTest {
    private static final int DELTA = 30;
    private static final int DELAY_MS = 100;
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
