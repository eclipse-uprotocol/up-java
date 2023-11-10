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

package org.eclipse.uprotocol.uuid.factory;

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
        final UUID uuid = UUIDFactory.Factories.UPROTOCOL.factory().create(now);
        final Optional<UUIDUtils.Version> version = UUIDUtils.getVersion(uuid);
        final Optional<Long> time = UUIDUtils.getTime(uuid);
        final Optional<byte[]> bytes = UUIDUtils.toBytes(uuid);
        final Optional<String> uuidString = UUIDUtils.toString(uuid);

        assertNotNull(uuid);
        assertTrue(UUIDUtils.isUProtocol(uuid));
        assertTrue(UUIDUtils.isUuid(uuid));
        assertFalse(UUIDUtils.isUuidv6(uuid));
        assertTrue(version.isPresent());
        assertTrue(time.isPresent());
        assertEquals(time.get(), now.toEpochMilli());

        assertTrue(bytes.isPresent());
        assertTrue(uuidString.isPresent());

        final Optional<UUID> uuid1 = UUIDUtils.fromBytes(bytes.get());

        assertTrue(uuid1.isPresent());
        assertEquals(uuid, uuid1.get());

        final Optional<UUID> uuid2 = UUIDUtils.fromString(uuidString.get());
        assertTrue(uuid2.isPresent());
        assertEquals(uuid, uuid2.get());
    }

    @Test
    @DisplayName("Test UUIDv8 Creation with null Instant")
    void test_uuidv8_creation_with_null_instant() {
        final UUID uuid = UUIDFactory.Factories.UPROTOCOL.factory().create(null);
        final Optional<UUIDUtils.Version> version = UUIDUtils.getVersion(uuid);
        final Optional<Long> time = UUIDUtils.getTime(uuid);
        final Optional<byte[]> bytes = UUIDUtils.toBytes(uuid);
        final Optional<String> uuidString = UUIDUtils.toString(uuid);

        assertNotNull(uuid);
        assertTrue(UUIDUtils.isUProtocol(uuid));
        assertTrue(UUIDUtils.isUuid(uuid));
        assertFalse(UUIDUtils.isUuidv6(uuid));
        assertTrue(version.isPresent());
        assertTrue(time.isPresent());
        assertTrue(bytes.isPresent());
        assertTrue(uuidString.isPresent());

        final Optional<UUID> uuid1 = UUIDUtils.fromBytes(bytes.get());

        assertTrue(uuid1.isPresent());
        assertEquals(uuid, uuid1.get());

        final Optional<UUID> uuid2 = UUIDUtils.fromString(uuidString.get());
        assertTrue(uuid2.isPresent());
        assertEquals(uuid, uuid2.get());
    }


    @Test
    @DisplayName("Test UUIDv8 overflow")
    void test_uuidv8_overflow() {
        final List<UUID> uuidList = new ArrayList<>();
        final int MAX_COUNT = 4095;

        // Build UUIDs above MAX_COUNT (4095) so we can test the limits
        final Instant now = Instant.now();
        for (int i = 0; i < MAX_COUNT * 2; i++) {
            uuidList.add(UUIDFactory.Factories.UPROTOCOL.factory().create(now));

            // Time should be the same as the 1st
            assertEquals(UUIDUtils.getTime(uuidList.get(0)), UUIDUtils.getTime(uuidList.get(i)));

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
        final UUID uuid = UUIDFactory.Factories.UUIDV6.factory().create(now);
        final Optional<UUIDUtils.Version> version = UUIDUtils.getVersion(uuid);
        final Optional<Long> time = UUIDUtils.getTime(uuid);
        final Optional<byte[]> bytes = UUIDUtils.toBytes(uuid);
        final Optional<String> uuidString = UUIDUtils.toString(uuid);

        assertNotNull(uuid);
        assertTrue(UUIDUtils.isUuidv6(uuid));
        assertTrue(UUIDUtils.isUuid(uuid));
        assertFalse(UUIDUtils.isUProtocol(uuid));
        assertTrue(version.isPresent());
        assertTrue(time.isPresent());
        assertEquals(time.get(), now.toEpochMilli());
        assertTrue(bytes.isPresent());
        assertTrue(uuidString.isPresent());

        final Optional<UUID> uuid1 = UUIDUtils.fromBytes(bytes.get());

        assertTrue(uuid1.isPresent());
        assertEquals(uuid, uuid1.get());

        final Optional<UUID> uuid2 = UUIDUtils.fromString(uuidString.get());
        assertTrue(uuid2.isPresent());
        assertEquals(uuid, uuid2.get());
    }

    @Test
    @DisplayName("Test UUIDv6 creation with null Instant")
    void test_uuidv6_creation_with_null_instant() {
        final UUID uuid = UUIDFactory.Factories.UUIDV6.factory().create(null);
        final Optional<UUIDUtils.Version> version = UUIDUtils.getVersion(uuid);
        final Optional<Long> time = UUIDUtils.getTime(uuid);
        final Optional<byte[]> bytes = UUIDUtils.toBytes(uuid);
        final Optional<String> uuidString = UUIDUtils.toString(uuid);

        assertNotNull(uuid);
        assertTrue(UUIDUtils.isUuidv6(uuid));
        assertFalse(UUIDUtils.isUProtocol(uuid));
        assertTrue(UUIDUtils.isUuid(uuid));
        assertTrue(version.isPresent());
        assertTrue(time.isPresent());
        assertTrue(bytes.isPresent());
        assertTrue(uuidString.isPresent());

        final Optional<UUID> uuid1 = UUIDUtils.fromBytes(bytes.get());

        assertTrue(uuid1.isPresent());
        assertEquals(uuid, uuid1.get());

        final Optional<UUID> uuid2 = UUIDUtils.fromString(uuidString.get());
        assertTrue(uuid2.isPresent());
        assertEquals(uuid, uuid2.get());
    }

    @Test
    @DisplayName("Test UUIDUtils for Random UUID")
    void test_uuidutils_for_random_uuid() {
        final java.util.UUID uuid_java = java.util.UUID.randomUUID();
        final UUID uuid = UUID.newBuilder().setMsb(uuid_java.getMostSignificantBits())
                .setLsb(uuid_java.getLeastSignificantBits()).build();
        final Optional<UUIDUtils.Version> version = UUIDUtils.getVersion(uuid);
        final Optional<Long> time = UUIDUtils.getTime(uuid);
        final Optional<byte[]> bytes = UUIDUtils.toBytes(uuid);
        final Optional<String> uuidString = UUIDUtils.toString(uuid);

        assertNotNull(uuid);
        assertFalse(UUIDUtils.isUuidv6(uuid));
        assertFalse(UUIDUtils.isUProtocol(uuid));
        assertFalse(UUIDUtils.isUuid(uuid));
        assertTrue(version.isPresent());
        assertFalse(time.isPresent());
        assertTrue(bytes.isPresent());
        assertTrue(uuidString.isPresent());


        final Optional<UUID> uuid1 = UUIDUtils.fromBytes(bytes.get());

        assertTrue(uuid1.isPresent());
        assertEquals(uuid, uuid1.get());

        final Optional<UUID> uuid2 = UUIDUtils.fromString(uuidString.get());
        assertTrue(uuid2.isPresent());
        assertEquals(uuid, uuid2.get());
    }

    @Test
    @DisplayName("Test UUIDUtils for empty UUID")
    void test_uuidutils_for_empty_uuid() {
        final UUID uuid =  UUID.newBuilder().setMsb(0L).setLsb(0L).build();
        final Optional<UUIDUtils.Version> version = UUIDUtils.getVersion(uuid);
        final Optional<Long> time = UUIDUtils.getTime(uuid);
        final Optional<byte[]> bytes = UUIDUtils.toBytes(uuid);
        final Optional<String> uuidString = UUIDUtils.toString(uuid);

        assertNotNull(uuid);
        assertFalse(UUIDUtils.isUuidv6(uuid));
        assertFalse(UUIDUtils.isUProtocol(uuid));
        assertTrue(version.isPresent());
        assertEquals(version.get(), UUIDUtils.Version.VERSION_UNKNOWN);
        assertFalse(time.isPresent());
        assertTrue(bytes.isPresent());
        assertTrue(uuidString.isPresent());
        assertFalse(UUIDUtils.isUuidv6(null));
        assertFalse(UUIDUtils.isUProtocol(null));
        assertFalse(UUIDUtils.isUuid(null));

        final Optional<UUID> uuid1 = UUIDUtils.fromBytes(bytes.get());

        assertTrue(uuid1.isPresent());
        assertEquals(uuid, uuid1.get());

        final Optional<UUID> uuid2 = UUIDUtils.fromString(uuidString.get());
        assertTrue(uuid2.isPresent());
        assertEquals(uuid, uuid2.get());
    }

    @Test
    @DisplayName("Test UUIDUtils for a null UUID")
    void test_uuidutils_for_null_uuid() {
        assertFalse(UUIDUtils.getVersion(null).isPresent());
        assertFalse(UUIDUtils.toBytes(null).isPresent());
        assertFalse(UUIDUtils.toString(null).isPresent());
        assertFalse(UUIDUtils.isUuidv6(null));
        assertFalse(UUIDUtils.isUProtocol(null));
        assertFalse(UUIDUtils.isUuid(null));
        assertFalse(UUIDUtils.getTime(null).isPresent());
    }

    @Test
    @DisplayName("Test UUIDUtils fromString an invalid built UUID")
    void test_uuidutils_from_invalid_uuid() {
        final UUID uuid = UUID.newBuilder().setMsb(9 << 12).setLsb(0L).build(); // Invalid UUID type

        assertFalse(UUIDUtils.getVersion(uuid).isPresent());
        assertFalse(UUIDUtils.getTime(uuid).isPresent());
        assertTrue(UUIDUtils.toBytes(uuid).isPresent());
        assertTrue(UUIDUtils.toString(uuid).isPresent());
        assertFalse(UUIDUtils.isUuidv6(uuid));
        assertFalse(UUIDUtils.isUProtocol(uuid));
        assertFalse(UUIDUtils.isUuid(uuid));
        assertFalse(UUIDUtils.getTime(uuid).isPresent());
    }


    @Test
    @DisplayName("Test UUIDUtils fromString with invalid string")
    void test_uuidutils_fromstring_with_invalid_string() {
        final Optional<UUID> uuid = UUIDUtils.fromString(null);
        assertFalse(uuid.isPresent());
        final Optional<UUID> uuid1 = UUIDUtils.fromString("");
        assertFalse(uuid1.isPresent());
    }

    @Test
    @DisplayName("Test UUIDUtils fromBytes with invalid bytes")
    void test_uuidutils_frombytes_with_invalid_bytes() {
        final Optional<UUID> uuid = UUIDUtils.fromBytes(null);
        assertFalse(uuid.isPresent());
        final Optional<UUID> uuid1 = UUIDUtils.fromBytes(new byte[0]);
        assertFalse(uuid1.isPresent());
    }

    @Test
    @DisplayName("Test Create UProtocol UUID in the past")
    void test_create_uprotocol_uuid_in_the_past() {

        final Instant past = Instant.now().minusSeconds(10);
        final UUID uuid = UUIDFactory.Factories.UPROTOCOL.factory().create(past);

        final Optional<Long> time = UUIDUtils.getTime(uuid);

        assertTrue(UUIDUtils.isUProtocol(uuid));
        assertTrue(UUIDUtils.isUuid(uuid));

        assertTrue(time.isPresent());
        assertEquals(time.get(), past.toEpochMilli());

    }

    @Test
    @DisplayName("Test Create UProtocol UUID with different time values")
    void test_create_uprotocol_uuid_with_different_time_values() throws InterruptedException {

        final UUID uuid = UUIDFactory.Factories.UPROTOCOL.factory().create();
        Thread.sleep(10);
        final UUID uuid1 = UUIDFactory.Factories.UPROTOCOL.factory().create();

        final Optional<Long> time = UUIDUtils.getTime(uuid);
        final Optional<Long> time1 = UUIDUtils.getTime(uuid1);

        assertTrue(UUIDUtils.isUProtocol(uuid));
        assertTrue(UUIDUtils.isUuid(uuid));
        assertTrue(UUIDUtils.isUProtocol(uuid1));
        assertTrue(UUIDUtils.isUuid(uuid1));

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
            uuidv8List.add(UUIDFactory.Factories.UPROTOCOL.factory().create());
        }
        final Duration v8Diff = Duration.between(start, Instant.now());

        start = Instant.now();
        for (int i = 0; i < MAX_COUNT; i++) {
            uuidv6List.add(UUIDFactory.Factories.UUIDV6.factory().create());
        }
        final Duration v6Diff = Duration.between(start, Instant.now());
        System.out.println(
                "UUIDv8:[" + v8Diff.toNanos() / MAX_COUNT + "ns]" + " UUIDv6:[" + v6Diff.toNanos() / MAX_COUNT + "ns]");
    }
}

