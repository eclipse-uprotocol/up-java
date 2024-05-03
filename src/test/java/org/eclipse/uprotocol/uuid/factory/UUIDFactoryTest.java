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

import org.eclipse.uprotocol.uuid.serializer.UuidSerializer;
import org.eclipse.uprotocol.v1.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class UUIDFactoryTest {

    @Test
    @DisplayName("Test UUIDv8 Creation")
    void test_uuidv8_creation() {
        final Instant now = Instant.now();
        final UUID uuid = UuidFactory.Factories.UPROTOCOL.factory().create(now);
        final Optional<UuidUtils.Version> version = UuidUtils.getVersion(uuid);
        final Optional<Long> time = UuidUtils.getTime(uuid);
        final String uuidString = UuidSerializer.serialize(uuid);

        assertNotNull(uuid);
        assertTrue(UuidUtils.isUProtocol(uuid));
        assertTrue(UuidUtils.isUuid(uuid));
        assertFalse(UuidUtils.isUuidv6(uuid));
        assertTrue(version.isPresent());
        assertTrue(time.isPresent());
        assertEquals(time.get(), now.toEpochMilli());

        assertFalse(uuidString.isBlank());

        final UUID uuid2 = UuidSerializer.deserialize(uuidString);
        assertFalse(uuid2.equals(UUID.getDefaultInstance()));
        assertEquals(uuid, uuid2);
    }

    @Test
    @DisplayName("Test UUIDv8 Creation with null Instant")
    void test_uuidv8_creation_with_null_instant() {
        final UUID uuid = UuidFactory.Factories.UPROTOCOL.factory().create(null);
        final Optional<UuidUtils.Version> version = UuidUtils.getVersion(uuid);
        final Optional<Long> time = UuidUtils.getTime(uuid);
        final String uuidString = UuidSerializer.serialize(uuid);

        assertNotNull(uuid);
        assertTrue(UuidUtils.isUProtocol(uuid));
        assertTrue(UuidUtils.isUuid(uuid));
        assertFalse(UuidUtils.isUuidv6(uuid));
        assertTrue(version.isPresent());
        assertTrue(time.isPresent());
        assertFalse(uuidString.isBlank());

        final UUID uuid2 = UuidSerializer.deserialize(uuidString);
        
        assertFalse(uuid2.equals(UUID.getDefaultInstance()));
        assertEquals(uuid, uuid2);
    }


    @Test
    @DisplayName("Test UUIDv8 overflow")
    void test_uuidv8_overflow() {
        final List<UUID> uuidList = new ArrayList<>();
        final int MAX_COUNT = 4095;

        // Build UUIDs above MAX_COUNT (4095) so we can test the limits
        final Instant now = Instant.now();
        for (int i = 0; i < MAX_COUNT * 2; i++) {
            uuidList.add(UuidFactory.Factories.UPROTOCOL.factory().create(now));

            // Time should be the same as the 1st
            assertEquals(UuidUtils.getTime(uuidList.get(0)), UuidUtils.getTime(uuidList.get(i)));

            // Random should always remain the same be the same
            assertEquals(uuidList.get(0).getLsb(), uuidList.get(i).getLsb());
            if (i > MAX_COUNT) {
                assertEquals(uuidList.get(MAX_COUNT).getMsb(), uuidList.get(i).getMsb());
            }
        }
    }

    @Test
    @DisplayName("Test UUIDv6 creation with Instance")
    void test_uuidv6_creation_with_instant() {
        final Instant now = Instant.now();
        final UUID uuid = UuidFactory.Factories.UUIDV6.factory().create(now);
        final Optional<UuidUtils.Version> version = UuidUtils.getVersion(uuid);
        final Optional<Long> time = UuidUtils.getTime(uuid);
        final String uuidString = UuidSerializer.serialize(uuid);

        assertNotNull(uuid);
        assertTrue(UuidUtils.isUuidv6(uuid));
        assertTrue(UuidUtils.isUuid(uuid));
        assertFalse(UuidUtils.isUProtocol(uuid));
        assertTrue(version.isPresent());
        assertTrue(time.isPresent());
        assertEquals(time.get(), now.toEpochMilli());
        assertFalse(uuidString.isBlank());

        final UUID uuid2 = UuidSerializer.deserialize(uuidString);
        assertFalse(uuid2.equals(UUID.getDefaultInstance()));
        assertEquals(uuid, uuid2);    }

    @Test
    @DisplayName("Test UUIDv6 creation with null Instant")
    void test_uuidv6_creation_with_null_instant() {
        final UUID uuid = UuidFactory.Factories.UUIDV6.factory().create(null);
        final Optional<UuidUtils.Version> version = UuidUtils.getVersion(uuid);
        final Optional<Long> time = UuidUtils.getTime(uuid);
        final String uuidString = UuidSerializer.serialize(uuid);

        assertNotNull(uuid);
        assertTrue(UuidUtils.isUuidv6(uuid));
        assertFalse(UuidUtils.isUProtocol(uuid));
        assertTrue(UuidUtils.isUuid(uuid));
        assertTrue(version.isPresent());
        assertTrue(time.isPresent());
        assertFalse(uuidString.isBlank());

        final UUID uuid2 = UuidSerializer.deserialize(uuidString);
        assertFalse(uuid2.equals(UUID.getDefaultInstance()));
        assertEquals(uuid, uuid2);
    }

    @Test
    @DisplayName("Test UUIDUtils for Random UUID")
    void test_uuidutils_for_random_uuid() {
        final java.util.UUID uuid_java = java.util.UUID.randomUUID();
        final UUID uuid = UUID.newBuilder().setMsb(uuid_java.getMostSignificantBits())
                .setLsb(uuid_java.getLeastSignificantBits()).build();
        final Optional<UuidUtils.Version> version = UuidUtils.getVersion(uuid);
        final Optional<Long> time = UuidUtils.getTime(uuid);
        final String uuidString = UuidSerializer.serialize(uuid);

        assertNotNull(uuid);
        assertFalse(UuidUtils.isUuidv6(uuid));
        assertFalse(UuidUtils.isUProtocol(uuid));
        assertFalse(UuidUtils.isUuid(uuid));
        assertTrue(version.isPresent());
        assertFalse(time.isPresent());
        assertFalse(uuidString.isBlank());

        final UUID uuid2 = UuidSerializer.deserialize(uuidString);
                
        assertFalse(uuid2.equals(UUID.getDefaultInstance()));
        assertEquals(uuid, uuid2);
    }

    @Test
    @DisplayName("Test UUIDUtils for empty UUID")
    void test_uuidutils_for_empty_uuid() {
        final UUID uuid =  UUID.newBuilder().setMsb(0L).setLsb(0L).build();
        final Optional<UuidUtils.Version> version = UuidUtils.getVersion(uuid);
        final Optional<Long> time = UuidUtils.getTime(uuid);
        final String uuidString = UuidSerializer.serialize(uuid);

        assertNotNull(uuid);
        assertFalse(UuidUtils.isUuidv6(uuid));
        assertFalse(UuidUtils.isUProtocol(uuid));
        assertTrue(version.isPresent());
        assertEquals(version.get(), UuidUtils.Version.VERSION_UNKNOWN);
        assertFalse(time.isPresent());
        assertFalse(uuidString.isBlank());
        assertFalse(UuidUtils.isUuidv6(null));
        assertFalse(UuidUtils.isUProtocol(null));
        assertFalse(UuidUtils.isUuid(null));

        final UUID uuid2 = UuidSerializer.deserialize(uuidString);
        assertTrue(uuid2.equals(UUID.getDefaultInstance()));
        assertEquals(uuid, uuid2);
    }

    @Test
    @DisplayName("Test UUIDUtils for a null UUID")
    void test_uuidutils_for_null_uuid() {
        assertFalse(UuidUtils.getVersion(null).isPresent());
        assertTrue(UuidSerializer.serialize(null).isBlank());
        assertFalse(UuidUtils.isUuidv6(null));
        assertFalse(UuidUtils.isUProtocol(null));
        assertFalse(UuidUtils.isUuid(null));
        assertFalse(UuidUtils.getTime(null).isPresent());
    }

    @Test
    @DisplayName("Test UUIDUtils fromString an invalid built UUID")
    void test_uuidutils_from_invalid_uuid() {
        final UUID uuid = UUID.newBuilder().setMsb(9 << 12).setLsb(0L).build(); // Invalid UUID type

        assertFalse(UuidUtils.getVersion(uuid).isPresent());
        assertFalse(UuidUtils.getTime(uuid).isPresent());
        assertFalse(UuidSerializer.serialize(uuid).isBlank());
        assertFalse(UuidUtils.isUuidv6(uuid));
        assertFalse(UuidUtils.isUProtocol(uuid));
        assertFalse(UuidUtils.isUuid(uuid));
        assertFalse(UuidUtils.getTime(uuid).isPresent());
    }


    @Test
    @DisplayName("Test UUIDUtils fromString with invalid string")
    void test_uuidutils_fromstring_with_invalid_string() {
        final UUID uuid = UuidSerializer.deserialize(null);
        assertTrue(uuid.equals(UUID.getDefaultInstance()));
        final UUID uuid1 = UuidSerializer.deserialize("");
        assertTrue(uuid1.equals(UUID.getDefaultInstance()));
    }


    @Test
    @DisplayName("Test Create UProtocol UUID in the past")
    void test_create_uprotocol_uuid_in_the_past() {

        final Instant past = Instant.now().minusSeconds(10);
        final UUID uuid = UuidFactory.Factories.UPROTOCOL.factory().create(past);

        final Optional<Long> time = UuidUtils.getTime(uuid);

        assertTrue(UuidUtils.isUProtocol(uuid));
        assertTrue(UuidUtils.isUuid(uuid));

        assertTrue(time.isPresent());
        assertEquals(time.get(), past.toEpochMilli());

    }

    @Test
    @DisplayName("Test Create UProtocol UUID with different time values")
    void test_create_uprotocol_uuid_with_different_time_values() throws InterruptedException {

        final UUID uuid = UuidFactory.Factories.UPROTOCOL.factory().create();
        Thread.sleep(10);
        final UUID uuid1 = UuidFactory.Factories.UPROTOCOL.factory().create();

        final Optional<Long> time = UuidUtils.getTime(uuid);
        final Optional<Long> time1 = UuidUtils.getTime(uuid1);

        assertTrue(UuidUtils.isUProtocol(uuid));
        assertTrue(UuidUtils.isUuid(uuid));
        assertTrue(UuidUtils.isUProtocol(uuid1));
        assertTrue(UuidUtils.isUuid(uuid1));

        assertTrue(time.isPresent());
        assertNotEquals(time.get(), time1.get());

    }

    @Test
    @DisplayName("Test Create both UUIDv6 and v8 to compare performance")
    void test_create_both_uuidv6_and_v8_to_compare_performance() throws InterruptedException {
        final List<UUID> uuidv6List = new ArrayList<>();
        final List<UUID> uuidv8List = new ArrayList<>();
        final int MAX_COUNT = 10000;

        Instant start = Instant.now();
        for (int i = 0; i < MAX_COUNT; i++) {
            uuidv8List.add(UuidFactory.Factories.UPROTOCOL.factory().create());
        }
        final Duration v8Diff = Duration.between(start, Instant.now());

        start = Instant.now();
        for (int i = 0; i < MAX_COUNT; i++) {
            uuidv6List.add(UuidFactory.Factories.UUIDV6.factory().create());
        }
        final Duration v6Diff = Duration.between(start, Instant.now());
        System.out.println(
                "UUIDv8:[" + v8Diff.toNanos() / MAX_COUNT + "ns]" + " UUIDv6:[" + v6Diff.toNanos() / MAX_COUNT + "ns]");
    }
}

