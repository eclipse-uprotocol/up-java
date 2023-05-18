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

package org.eclipse.uprotocol.uuid.factory;

import org.eclipse.uprotocol.uuid.factory.UUIDFactory;
import org.eclipse.uprotocol.uuid.factory.UUIDUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class UUIDFactoryTest {
    @Test
    @DisplayName("Test Conversion from string to obj")
    public void test_string_to_obj_conversions() {
        String str1 = "01868381-1590-8000-cfe2-68135f43b363";
        UUID uuid = UUIDUtils.fromString(str1);
        String str2 = uuid.toString();
        assertEquals(str1, str2);
    }
    @Test
    @DisplayName("Test Conversion from obj to string")
    public void test_obj_to_string_conversions() {
        UUID uuid1 = UUIDFactory.Factories.UPROTOCOL.factory().create();
        String str1 = uuid1.toString();
        UUID uuid2 = UUID.fromString(str1);
        assertEquals(str1, uuid2.toString());
    }

    @Test
    @DisplayName("Test consistent random")
    public void test_uuid_for_constant_random() {
        UUID uuid1 = UUIDFactory.Factories.UPROTOCOL.factory().create(Instant.now());
        UUID uuid2 = UUIDFactory.Factories.UPROTOCOL.factory().create(Instant.now());
        assertEquals(uuid1.getLeastSignificantBits(), uuid2.getLeastSignificantBits());
    }

    @Test
    @DisplayName("test counter overflow size")
    public void test_uuid_create_test_counters() {
        List<UUID> uuid = new ArrayList<>();
        // Keep time the same to text the counter
        final Instant now = Instant.now();
        try {
            for (int i = 0; i < 4096; i++) {
                uuid.add(UUIDFactory.Factories.UPROTOCOL.factory().create(now));

                // Time should be the same as the 1st
                assertEquals(UUIDUtils.getTime(uuid.get(0)), UUIDUtils.getTime(uuid.get(i)));

                // Random should always remain the same be the same
                assertEquals(uuid.get(0).getLeastSignificantBits(), uuid.get(i).getLeastSignificantBits());
            }

            uuid.add(UUIDFactory.Factories.UPROTOCOL.factory().create(now));
        }
        catch (Exception e) {
            assertEquals("Counters out of bounds", e.getMessage().toString());
        }
    }

    @Test
    @DisplayName("Test Conversion to/from bytes")
    public void test_uuid_byte_obj_conversions() {
        final UUID uuid1 = UUIDFactory.Factories.UPROTOCOL.factory().create();
        final byte[] bytes = UUIDUtils.toBytes(uuid1);
        final UUID uuid2 = UUIDUtils.fromBytes(bytes);
        assertArrayEquals(bytes, UUIDUtils.toBytes(uuid2));
        assertEquals(uuid1.toString(), uuid1.toString());
    }

    @Test
    @DisplayName("Test UUIDv6 Builder")
    public void test_uuid6_byte_obj_conversions() {
        final UUID uuid1 = UUIDFactory.Factories.UUIDV6.factory().create();
        final byte[] bytes = UUIDUtils.toBytes(uuid1);
        final UUID uuid2 = UUIDUtils.fromBytes(bytes);
        assertArrayEquals(bytes, UUIDUtils.toBytes(uuid2));
        assertEquals(uuid1.toString(), uuid1.toString());
    }

    @Test
    @DisplayName("Test multiple UUIDv6s Builder")
    public void test_uuid6_build_many() {
        List<UUID> uuid = new ArrayList<>();
        for (int i = 0; i < 4096; i++) {
            uuid.add(UUIDFactory.Factories.UUIDV6.factory().create());
        }
        try {
            uuid.add(UUIDFactory.Factories.UUIDV6.factory().create());
        }
        catch (Exception e) {
            assertEquals("Counters out of bounds", e.getMessage().toString());
        }
        assertNotEquals(UUIDUtils.getTime(uuid.get(0)), UUIDUtils.getTime(uuid.get(uuid.size()-1)));
    }

    @Test
    @DisplayName("Test UUIDv1")
    public void test_uuid1_gettime() {
        UUID uuid = UUID.randomUUID();
        try {
            UUIDUtils.getTime(uuid);
        } catch (Exception e) {
            assertEquals("Unsupported UUID", e.getMessage().toString());
        }
    }

    @Test
    @DisplayName("Test isUUID types")
    public void test_us_uuid_version_checks() {
        final UUID uuid1 = UUID.randomUUID();
        final UUID uuid2 = UUIDFactory.Factories.UUIDV6.factory().create();
        final UUID uuid3 = UUIDFactory.Factories.UPROTOCOL.factory().create();
        assertTrue(UUIDUtils.isUProtocol(uuid3));
        assertTrue(UUIDUtils.isUuidv6(uuid2));
        assertTrue(UUIDUtils.isUuid(uuid2));
        assertTrue(UUIDUtils.isUuid(uuid3));
        assertFalse(UUIDUtils.isUuid(uuid1));
    }

    @Test
    @DisplayName("Test size")
    public void test_uuid_size() {
        final UUID uuid1 = UUIDFactory.Factories.UPROTOCOL.factory().create();
        final byte[] bytes = UUIDUtils.toBytes(uuid1);

        String s = Base64.getEncoder().encodeToString(bytes);
        final byte[] bytes2 = Base64.getDecoder().decode(s);
        final UUID uuid2 = UUIDUtils.fromBytes(bytes2);
        System.out.println("Size of UUID as string is: " +
                uuid1.toString().length() + " Length in binary is: " + s.length() );
        assertArrayEquals(bytes, bytes2);
        assertEquals(uuid1.toString(), uuid2.toString());
    }

}

