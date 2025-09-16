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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.stream.Stream;

// [utest->dsn~uuid-spec~1]
// [utest->req~uuid-type~1]
class UuidUtilsTest {
    private static final int TTL = 10000;

    private UUID invalidUuid() {
        return UUID.newBuilder()
            .setMsb(0x0000000000006000L) // version 6
            .setLsb(0xC000000000000000L) // variant not RFC 9562
            .build();
    }

    private static Stream<Arguments> provideUuidsForIsUProtocol() {
        return Stream.of(
            Arguments.of(UUID.newBuilder()
                .setMsb(0x0000000000006000L)
                .setLsb(0xA000000000000000L)
                .build(), false),
            Arguments.of(UUID.newBuilder()
                .setMsb(0x0000000000007000L)
                .setLsb(0xA000000000000000L)
                .build(), true),
            Arguments.of(UUID.newBuilder()
                .setMsb(0x0000000000006000L)
                .setLsb(0xC000000000000000L)
                .build(), false),
            Arguments.of(UUID.newBuilder()
                .setMsb(0x0000000000007000L)
                .setLsb(0xC000000000000000L)
                .build(), false),
            Arguments.of(null, false)
        );
    }

    @ParameterizedTest(name = "Test isUProtocol {index} - {0}")
    @MethodSource("provideUuidsForIsUProtocol")
    void testIsUProtocol(UUID uuid, boolean expected) {
        assertEquals(expected, UuidUtils.isUProtocol(uuid));
    }

    private static Stream<Instant> provideCreationTimestamps() {
        return Stream.of(
            Instant.now().minusSeconds(60),
            Instant.now(),
            Instant.now().plusSeconds(60)
        );
    }

    @ParameterizedTest(name = "Test getting timestamp from UUID {index} - {arguments}")
    @MethodSource("provideCreationTimestamps")
    void testGetTime(Instant timestamp) {
        final UUID uuid = UuidFactory.create(timestamp);
        final var time = UuidUtils.getTime(uuid);
        assertEquals(timestamp.toEpochMilli(), time);
    }

    @Test
    void testGetTimeRejectsInvalidArgs() {
        assertThrows(NullPointerException.class, () -> UuidUtils.getTime(null));
        assertThrows(IllegalArgumentException.class, () -> UuidUtils.getTime(invalidUuid()));
    }

    private static Stream<Arguments> provideElapsedTimeTestCases() {
        final var now = Instant.now();
        return Stream.of(
            Arguments.of(now.minusSeconds(60), now, 60000),
            Arguments.of(now, now, 0),
            Arguments.of(now.plusSeconds(60), now, -60000),
            Arguments.of(now.minusMillis(50), now.plusMillis(100), 150)
        );
    }

    @ParameterizedTest(name = "Test getting elapsed time for UUID {index} - {arguments}")
    @MethodSource("provideElapsedTimeTestCases")
    void testGetElapsedTime(Instant creationTime, Instant referenceTime, long expectedElapsed) {
        final UUID uuid = UuidFactory.create(creationTime);
        assertEquals(expectedElapsed, UuidUtils.getElapsedTime(uuid, referenceTime));
    }

    @Test
    void testGetElapsedTimeUsesNowForReference() {
        var creationTime = Instant.now().minusSeconds(30);
        final UUID uuid = UuidFactory.create(creationTime);
        final var elapsed = UuidUtils.getElapsedTime(uuid, null);
        assertTrue(elapsed >= 30000);
        assertTrue(elapsed < 31000);
    }

    @Test
    void testGetElapsedTimeRejectsInvalidArgs() {
        assertThrows(NullPointerException.class, () -> UuidUtils.getElapsedTime(null, Instant.now()));
        assertThrows(IllegalArgumentException.class, () -> UuidUtils.getElapsedTime(invalidUuid(), Instant.now()));
    }

    private static Stream<Arguments> provideRemainingTimeTestCases() {
        final var now = Instant.now();
        return Stream.of(
            Arguments.of(now.minusSeconds(60), 40_000, now, 0),
            Arguments.of(now.minusSeconds(60), 60_000, now, 0),
            Arguments.of(now.minusSeconds(60), 80_000, now, 20_000),
            Arguments.of(now.plusSeconds(10), 70_000, now.plusSeconds(79), 1_000)
        );
    }

    @ParameterizedTest(name = "Test getting remaining time for UUID {index} - {arguments}")
    @MethodSource("provideRemainingTimeTestCases")
    void testGetRemainingTime(Instant creationTime, int ttl, Instant referenceTime, long expectedRemaining) {
        final UUID id = UuidFactory.create(creationTime);
        assertEquals(expectedRemaining, UuidUtils.getRemainingTime(id, ttl, referenceTime));
    }

    @Test
    void testGetRemainingTimeUsesNowForReference() {
        var creationTime = Instant.now().minusSeconds(10);
        final UUID uuid = UuidFactory.create(creationTime);
        final var remaining = UuidUtils.getRemainingTime(uuid, 15_000, null);
        assertTrue(remaining > 4000);
        assertTrue(remaining <= 5000);
    }

    @Test
    void testGetRemainingTimeRejectsInvalidArgs() {
        assertThrows(NullPointerException.class, () -> UuidUtils.getRemainingTime(null, TTL, Instant.now()));
        assertThrows(
            IllegalArgumentException.class,
            () -> UuidUtils.getRemainingTime(invalidUuid(), TTL, Instant.now())
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> UuidUtils.getRemainingTime(UuidFactory.create(), 0, Instant.now().plusSeconds(10))
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> UuidUtils.getRemainingTime(UuidFactory.create(), -1, Instant.now().plusSeconds(10))
        );
    }

    private static Stream<Arguments> provideIsExpiredTestCases() {
        final var now = Instant.now();
        return Stream.of(
            Arguments.of(now.minusSeconds(60), 0, now, false),
            Arguments.of(now.minusSeconds(60), 40_000, now, true),
            Arguments.of(now.minusSeconds(60), 60_000, now, true),
            Arguments.of(now.minusSeconds(60), 80_000, now, false),
            Arguments.of(now.plusSeconds(10), 70_000, now.plusSeconds(79), false)
        );
    }

    @ParameterizedTest(name = "Test isExpired for UUID {index} - {arguments}")
    @MethodSource("provideIsExpiredTestCases")
    void testIsExpired(Instant creationTime, int ttl, Instant referenceTime, boolean expected) {
        final UUID id = UuidFactory.create(creationTime);
        assertEquals(expected, UuidUtils.isExpired(id, ttl, referenceTime));
    }

    @Test
    void testIsExpiredRejectsInvalidArgs() {
        assertThrows(NullPointerException.class, () -> UuidUtils.isExpired(null, TTL, Instant.now()));
        assertThrows(IllegalArgumentException.class, () -> UuidUtils.isExpired(invalidUuid(), TTL, Instant.now()));
        assertThrows(
            IllegalArgumentException.class,
            () -> UuidUtils.isExpired(UuidFactory.create(), -3, Instant.now())
        );
    }
}
