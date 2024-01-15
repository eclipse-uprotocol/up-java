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

package org.eclipse.uprotocol.uri.builder;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.eclipse.uprotocol.v1.UResource;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.uprotocol.example.hello_world.v1.Timer;

public class UResourceBuilderTest {
    
    @Test
    @DisplayName("Test building UResource from Timer.Resources.one_second ProtobufMessageEnum")
    public void test_build_uresource_from_protobuf_message_enum() {
        UResource resource = UResourceBuilder.fromProto(Timer.Resources.one_second);
        assertEquals(resource.getName(), "one_second");
        assertEquals(resource.getId(), 1000);
        assertEquals(resource.getMessage(), "Timer");
    }
}
