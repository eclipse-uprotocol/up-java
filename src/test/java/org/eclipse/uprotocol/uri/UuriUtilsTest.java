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

package org.eclipse.uprotocol.uri;
import org.junit.jupiter.api.Test;
import org.eclipse.uprotocol.v1.UAuthority;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UuriUtilsTest {
    @Test
    void testExtractVinFromUAuthorityWithValidUAuthority() {
        UAuthority uAuthority = UAuthority.newBuilder().setName("1gk12d1t2n10339dc.veh").build();
        Optional<String> result = UuriUtils.extractVinFromUAuthority(uAuthority);
        assertTrue(result.isPresent());
        assertEquals("1gk12d1t2n10339dc", result.get());
    }

    @Test
    void testExtractVinFromUAuthorityWithValidUAuthority_Vcu_start() {
        UAuthority uAuthority = UAuthority.newBuilder().setName("vcu.1gk12d1t2n10339dc").build();
        Optional<String> result = UuriUtils.extractVinFromUAuthority(uAuthority);
        assertTrue(result.isPresent());
        assertEquals("1gk12d1t2n10339dc", result.get());
    }

    @Test
    void testExtractVinFromUAuthorityWithNullUAuthority() {
        Optional<String> result = UuriUtils.extractVinFromUAuthority(null);
        assertFalse(result.isPresent());
    }

    @Test
    void testExtractVinFromUAuthorityWithUAuthorityWithoutName() {
        UAuthority uAuthority = UAuthority.newBuilder().build();
        Optional<String> result = UuriUtils.extractVinFromUAuthority(uAuthority);
        assertFalse(result.isPresent());
    }

}
