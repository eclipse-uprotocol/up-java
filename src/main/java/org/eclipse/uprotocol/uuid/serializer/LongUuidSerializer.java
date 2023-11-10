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

 package org.eclipse.uprotocol.uuid.serializer;

import org.eclipse.uprotocol.v1.UUID;

/**
 * UUID Serializer interface used to serialize/deserialize UUIDs to/from a string
 */
public class LongUuidSerializer implements UuidSerializer<String> {
    private static final LongUuidSerializer INSTANCE = new LongUuidSerializer();

    private LongUuidSerializer(){}

    public static LongUuidSerializer instance() {
        return INSTANCE;
    }

    @Override
    public UUID deserialize(String stringUuid) {
        if (stringUuid == null || stringUuid.isBlank()) {
            return UUID.getDefaultInstance();
        }
        try {
            java.util.UUID uuid_java = java.util.UUID.fromString(stringUuid);
            return UUID.newBuilder().setMsb(uuid_java.getMostSignificantBits())
                    .setLsb(uuid_java.getLeastSignificantBits()).build();
        } catch (IllegalArgumentException e) {
            return UUID.getDefaultInstance();
        }
    }

    @Override
    public String serialize(UUID uuid) {
        return uuid == null ? new String() : new java.util.UUID(uuid.getMsb(), uuid.getLsb()).toString();
    }
    
}
