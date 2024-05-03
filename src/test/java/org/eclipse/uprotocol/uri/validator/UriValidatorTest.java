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

package org.eclipse.uprotocol.uri.validator;


import org.eclipse.uprotocol.uri.serializer.UriSerializer;

import org.eclipse.uprotocol.v1.UUri;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;



import static org.junit.jupiter.api.Assertions.assertTrue;

class UriValidatorTest {

    @Test
    @DisplayName("Test isEmpty with null UUri")
    public void test_isEmpty_with_null_UUri() {
        assertTrue(UriValidator.isEmpty(null));
    }

    @Test
    @DisplayName("Test isEmpty with default UUri")
    public void test_isEmpty_with_default_UUri() {
        assertTrue(UriValidator.isEmpty(UUri.getDefaultInstance()));
    }
    
    @Test
    @DisplayName("Test isEmpty for non empty UUri")
    public void test_isEmpty_for_non_empty_UUri() {
        UUri uri = UUri.newBuilder()
            .setAuthorityName("myAuthority")
            .setUeId(0)
            .setUeVersionMajor(1)
            .setResourceId(1).build();
        assertTrue(!UriValidator.isEmpty(uri));
    }

    @Test
    @DisplayName("Test isEmpty UUri for empty built UUri")
    public void test_isEmpty_UUri_for_empty_built_UUri() {
        UUri uri = UUri.newBuilder().build();
        assertTrue(UriValidator.isEmpty(uri));
    }

    @Test
    @DisplayName("Test isRpcMethod with null UUri")
    public void test_isRpcMethod_with_null_UUri() {
        assertTrue(!UriValidator.isRpcMethod(null));
    }

    @Test
    @DisplayName("Test isRpcMethod with default UUri")
    public void test_isRpcMethod_with_default_UUri() {
        assertTrue(!UriValidator.isRpcMethod(UUri.getDefaultInstance()));
    }

    @Test
    @DisplayName("Test isRpcMethod with UUri having resourceId less than MIN_TOPIC_ID")
    public void test_isRpcMethod_with_UUri_having_resourceId_less_than_MIN_TOPIC_ID() {
        UUri uri = UUri.newBuilder()
            .setResourceId(0x7FFF).build();
        assertTrue(UriValidator.isRpcMethod(uri));
    }

    @Test
    @DisplayName("Test isRpcMethod with UUri having resourceId greater than MIN_TOPIC_ID")
    public void test_isRpcMethod_with_UUri_having_resourceId_greater_than_MIN_TOPIC_ID() {
        UUri uri = UUri.newBuilder()
            .setResourceId(0x8000).build();
        assertTrue(!UriValidator.isRpcMethod(uri));
    }

    @Test
    @DisplayName("Test isRpcMethod with UUri having resourceId equal to MIN_TOPIC_ID")
    public void test_isRpcMethod_with_UUri_having_resourceId_equal_to_MIN_TOPIC_ID() {
        UUri uri = UUri.newBuilder()
            .setResourceId(0x8000).build();
        assertTrue(!UriValidator.isRpcMethod(uri));
    }

    @Test
    @DisplayName("Test isRpcResponse with null UUri")
    public void test_isRpcResponse_with_null_UUri() {
        assertTrue(!UriValidator.isRpcResponse(null));
    }

    @Test
    @DisplayName("Test isRpcResponse with default UUri")
    public void test_isRpcResponse_with_default_UUri() {
        assertTrue(!UriValidator.isRpcResponse(UUri.getDefaultInstance()));
    }

    @Test
    @DisplayName("Test isRpcResponse with UUri having resourceId equal to RPC_RESPONSE_ID")
    public void test_isRpcResponse_with_UUri_having_resourceId_equal_to_RPC_RESPONSE_ID() {
        UUri uri = UUri.newBuilder()
            .setAuthorityName("hartley")
            .setUeId(1)
            .setUeVersionMajor(1)
            .setResourceId(UriValidator.RPC_RESPONSE_ID).build();
        assertTrue(UriValidator.isRpcResponse(uri));
    }

    @Test
    @DisplayName("Test isRpcResponse with UUri having resourceId not equal to RPC_RESPONSE_ID")
    public void test_isRpcResponse_with_UUri_having_resourceId_not_equal_to_RPC_RESPONSE_ID() {
        UUri uri = UUri.newBuilder()
            .setAuthorityName("hartley")
            .setUeId(1)
            .setUeVersionMajor(1)
            .setResourceId(1).build();
        assertTrue(!UriValidator.isRpcResponse(uri));
    }

    @Test
    @DisplayName("Test isRpcResponse with UUri having resourceId less than RPC_RESPONSE_ID")
    public void test_isRpcResponse_with_UUri_having_resourceId_less_than_RPC_RESPONSE_ID() {
        UUri uri = UUri.newBuilder()
            .setResourceId(-1).build();
        assertTrue(!UriValidator.isRpcResponse(uri));
    }

}
