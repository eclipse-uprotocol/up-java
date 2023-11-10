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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.eclipse.uprotocol.v1.UUID;


public class MicroUuidSerializer implements UuidSerializer<byte[]>{
    private static final MicroUuidSerializer INSTANCE = new MicroUuidSerializer();

    private MicroUuidSerializer(){}

    public static MicroUuidSerializer instance() {
        return INSTANCE;
    }

    
    @Override
    public UUID deserialize(byte[] uuid) {
        if (uuid == null || uuid.length != 16) {
            return UUID.getDefaultInstance();
        }

        final ByteBuffer byteBuffer = ByteBuffer.wrap(uuid);
        return UUID.newBuilder().setMsb(byteBuffer.getLong()).setLsb(byteBuffer.getLong()).build();
    }

    @Override
    public byte[] serialize(UUID uuid) {
        if (uuid == null) {
            return new byte[0];
        }
        byte[] b = new byte[16];
        return ByteBuffer.wrap(b).order(ByteOrder.BIG_ENDIAN).putLong(uuid.getMsb()).putLong(uuid.getLsb()).array();
    }
    
}
