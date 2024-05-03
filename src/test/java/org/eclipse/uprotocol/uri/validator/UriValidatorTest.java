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
package org.eclipse.uprotocol.uri.validator;


import org.eclipse.uprotocol.v1.UUri;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
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
        assertFalse(UriValidator.isRpcMethod(UUri.getDefaultInstance()));
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
    @DisplayName("Test isRpcResponse with UUri having resourceId equal to 0")
    public void test_isRpcResponse_with_UUri_having_resourceId_equal_to_0() {
        UUri uri = UUri.newBuilder()
            .setAuthorityName("hartley")
            .setUeId(1)
            .setUeVersionMajor(1)
            .setResourceId(0).build();
        assertTrue(UriValidator.isRpcResponse(uri));
    }

    @Test
    @DisplayName("Test isRpcResponse with UUri having resourceId not equal to 0")
    public void test_isRpcResponse_with_UUri_having_resourceId_not_equal_to_0() {
        UUri uri = UUri.newBuilder()
            .setAuthorityName("hartley")
            .setUeId(1)
            .setUeVersionMajor(1)
            .setResourceId(1).build();
        assertTrue(!UriValidator.isRpcResponse(uri));
    }

    @Test
    @DisplayName("Test isRpcResponse with UUri having resourceId less than 0")
    public void test_isRpcResponse_with_UUri_having_resourceId_less_than_0() {
        UUri uri = UUri.newBuilder()
            .setResourceId(-1).build();
        assertTrue(!UriValidator.isRpcResponse(uri));
    }

    @Test
    @DisplayName("Test isTopic with null UUri")
    public void test_isTopic_with_null_UUri() {
        assertFalse(UriValidator.isTopic(null));
    }

    @Test
    @DisplayName("Test isTopic with default UUri")
    public void test_isTopic_with_default_UUri() {
        assertFalse(UriValidator.isTopic(UUri.getDefaultInstance()));
    }

    @Test
    @DisplayName("Test isTopic with UUri having resourceId greater than 0")
    public void test_isTopic_with_UUri_having_resourceId_greater_than_0() {
        UUri uri = UUri.newBuilder()
            .setResourceId(1).build();
        assertFalse(UriValidator.isTopic(uri));
    }

    @Test
    @DisplayName("Test isTopic with UUri having resourceId greater than 0x8000")
    public void test_isTopic_with_UUri_having_resourceId_greater_than_0x8000() {
        UUri uri = UUri.newBuilder()
            .setResourceId(0x8001).build();
        assertTrue(UriValidator.isTopic(uri));
    }
    
    @Test
    @DisplayName("Test isRpcMethod should be false when resourceId is 0")
    public void test_isRpcMethod_should_be_false_when_resourceId_is_0() {
        UUri uri = UUri.newBuilder()
            .setUeId(1)
            .setResourceId(0).build();
        assertFalse(UriValidator.isRpcMethod(uri));
    }
}
