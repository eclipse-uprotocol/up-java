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
 * 
 * SPDX-FileType: SOURCE
 * SPDX-FileCopyrightText: 2024 General Motors GTO LLC
 * SPDX-License-Identifier: Apache-2.0
*/

package org.eclipse.uprotocol.uri.factory;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
 
import com.google.protobuf.Descriptors.ServiceDescriptor;
import static org.junit.jupiter.api.Assertions.assertEquals;
 
import org.eclipse.uprotocol.core.usubscription.v3.USubscriptionProto;
import org.eclipse.uprotocol.core.utwin.v1.UTwinProto;
import org.eclipse.uprotocol.core.udiscovery.v3.UDiscoveryProto;
import org.eclipse.uprotocol.v1.UEntity;

public class UEntityFactoryTest {
    @Test
    @DisplayName("Test build valid usubscription UEntity")
    public void test_build_valid_usubscription_uentity() {
        ServiceDescriptor descriptor = USubscriptionProto.getDescriptor().getServices().get(0);
        UEntity entity = UEntityFactory.fromProto(descriptor);

        assertEquals(entity.getName(), "core.usubscription");
        assertEquals(entity.getId(), 0);
        assertEquals(entity.getVersionMajor(), 3);
        assertEquals(entity.getVersionMinor(), 0);
    }

    @Test
    @DisplayName("Test build valid uDiscovery UEntity")
    public void test_build_valid_udiscovery_uentity() {
        ServiceDescriptor descriptor = UDiscoveryProto.getDescriptor().getServices().get(0);

        UEntity entity = UEntityFactory.fromProto(descriptor);

        assertEquals(entity.getName(), "core.udiscovery");
        assertEquals(entity.getId(), 1);
        assertEquals(entity.getVersionMajor(), 3);
        assertEquals(entity.getVersionMinor(), 0);
    }

    @Test
    @DisplayName("Test build valid uTwin UEntity")
    public void test_build_valid_utwin_uentity() {
        ServiceDescriptor descriptor = UTwinProto.getDescriptor().getServices().get(0);

        UEntity entity = UEntityFactory.fromProto(descriptor);

        assertEquals(entity.getName(), "core.utwin");
        assertEquals(entity.getId(), 26);
        assertEquals(entity.getVersionMajor(), 2);
        assertEquals(entity.getVersionMinor(), 0);
    }

    @Test
    @DisplayName("Test build passing empty descriptor")
    public void test_build_empty_descriptor() {
        ServiceDescriptor descriptor = null;

        UEntity entity = UEntityFactory.fromProto(descriptor);

        assertEquals(entity.getName(), "");
        assertEquals(entity.getId(), 0);
        assertEquals(entity.getVersionMajor(), 0);
        assertEquals(entity.getVersionMinor(), 0);
    }

    /*TODO: Need to add a non uService service descriptor (doesn't have the name, id, version, etc...) */
}