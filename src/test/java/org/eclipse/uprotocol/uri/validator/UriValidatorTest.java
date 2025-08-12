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


import org.eclipse.uprotocol.uri.serializer.UriSerializer;
import org.eclipse.uprotocol.v1.UUri;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UriValidatorTest {

    @Test
    @DisplayName("Test isEmpty with null UUri")
    public void testIsemptyWithNullUuri() {
        assertTrue(UriValidator.isEmpty(null));
    }

    @Test
    @DisplayName("Test isEmpty with default UUri")
    public void testIsemptyWithDefaultUuri() {
        assertTrue(UriValidator.isEmpty(UUri.getDefaultInstance()));
    }
    
    @Test
    @DisplayName("Test isEmpty for non empty UUri")
    public void testIsemptyForNonEmptyUuri() {
        UUri uri = UUri.newBuilder()
            .setAuthorityName("myAuthority")
            .setUeId(0)
            .setUeVersionMajor(1)
            .setResourceId(1).build();
        assertTrue(!UriValidator.isEmpty(uri));
    }

    @Test
    @DisplayName("Test isEmpty UUri for empty built UUri")
    public void testIsemptyUuriForEmptyBuiltUuri() {
        UUri uri = UUri.newBuilder().build();
        assertTrue(UriValidator.isEmpty(uri));
    }

    @Test
    @DisplayName("Test isRpcMethod with null UUri")
    public void testIsrpcmethodWithNullUuri() {
        assertTrue(!UriValidator.isRpcMethod(null));
    }

    @Test
    @DisplayName("Test isRpcMethod with default UUri")
    public void testIsrpcmethodWithDefaultUuri() {
        assertFalse(UriValidator.isRpcMethod(UUri.getDefaultInstance()));
    }

    @Test
    @DisplayName("Test isRpcMethod with UUri having resourceId less than MIN_TOPIC_ID")
    public void testIsrpcmethodWithUuriHavingResourceidLessThanMinTopicId() {
        UUri uri = UUri.newBuilder()
            .setResourceId(0x7FFF).build();
        assertTrue(UriValidator.isRpcMethod(uri));
    }

    @Test
    @DisplayName("Test isRpcMethod with UUri having resourceId greater than MIN_TOPIC_ID")
    public void testIsrpcmethodWithUuriHavingResourceidGreaterThanMinTopicId() {
        UUri uri = UUri.newBuilder()
            .setResourceId(0x8000).build();
        assertTrue(!UriValidator.isRpcMethod(uri));
    }

    @Test
    @DisplayName("Test isRpcMethod with UUri having resourceId equal to MIN_TOPIC_ID")
    public void testIsrpcmethodWithUuriHavingResourceidEqualToMinTopicId() {
        UUri uri = UUri.newBuilder()
            .setResourceId(0x8000).build();
        assertTrue(!UriValidator.isRpcMethod(uri));
    }

    @Test
    @DisplayName("Test isRpcResponse with null UUri")
    public void testIsrpcresponseWithNullUuri() {
        assertTrue(!UriValidator.isRpcResponse(null));
    }

    @Test
    @DisplayName("Test isRpcResponse with default UUri")
    public void testIsrpcresponseWithDefaultUuri() {
        assertTrue(!UriValidator.isRpcResponse(UUri.getDefaultInstance()));
    }

    @Test
    @DisplayName("Test isRpcResponse with UUri having resourceId equal to 0")
    public void testIsrpcresponseWithUuriHavingResourceidEqualTo0() {
        UUri uri = UUri.newBuilder()
            .setAuthorityName("hartley")
            .setUeId(1)
            .setUeVersionMajor(1)
            .setResourceId(0).build();
        assertTrue(UriValidator.isRpcResponse(uri));
    }

    @Test
    @DisplayName("Test isRpcResponse with UUri having resourceId not equal to 0")
    public void testIsrpcresponseWithUuriHavingResourceidNotEqualTo0() {
        UUri uri = UUri.newBuilder()
            .setAuthorityName("hartley")
            .setUeId(1)
            .setUeVersionMajor(1)
            .setResourceId(1).build();
        assertTrue(!UriValidator.isRpcResponse(uri));
    }

    @Test
    @DisplayName("Test isRpcResponse with UUri having resourceId less than 0")
    public void testIsrpcresponseWithUuriHavingResourceidLessThan0() {
        UUri uri = UUri.newBuilder()
            .setResourceId(-1).build();
        assertTrue(!UriValidator.isRpcResponse(uri));
    }

    @Test
    @DisplayName("Test isTopic with null UUri")
    public void testIstopicWithNullUuri() {
        assertFalse(UriValidator.isTopic(null));
    }

    @Test
    @DisplayName("Test isTopic with default UUri")
    public void testIstopicWithDefaultUuri() {
        assertFalse(UriValidator.isTopic(UUri.getDefaultInstance()));
    }

    @Test
    @DisplayName("Test isTopic with UUri having resourceId greater than 0")
    public void testIstopicWithUuriHavingResourceidGreaterThan0() {
        UUri uri = UUri.newBuilder()
            .setResourceId(1).build();
        assertFalse(UriValidator.isTopic(uri));
    }

    @Test
    @DisplayName("Test isTopic with UUri having resourceId greater than 0x8000")
    public void testIstopicWithUuriHavingResourceidGreaterThan0x8000() {
        UUri uri = UUri.newBuilder()
            .setResourceId(0x8001).build();
        assertTrue(UriValidator.isTopic(uri));
    }
    
    @Test
    @DisplayName("Test isRpcMethod should be false when resourceId is 0")
    public void testIsrpcmethodShouldBeFalseWhenResourceidIs0() {
        UUri uri = UUri.newBuilder()
            .setUeId(1)
            .setResourceId(0).build();
        assertFalse(UriValidator.isRpcMethod(uri));
    }

    @Test
    @DisplayName("Matches succeeds for identical URIs")
    public void testMatchesSucceedsForIdenticalUris() {
        UUri patternUri = UriSerializer.deserialize("//authority/A410/3/1003");
        UUri candidateUri = UriSerializer.deserialize("//authority/A410/3/1003");
        assertTrue(UriValidator.matches(patternUri, candidateUri));
    }

    @Test
    @DisplayName("Matches succeeds for pattern with wildcard authority")
    public void testMatchesSucceedsForPatternWithWildcardAuthority() {
        UUri patternUri = UriSerializer.deserialize("//*/A410/3/1003");
        UUri candidateUri = UriSerializer.deserialize("//authority/A410/3/1003");
        assertTrue(UriValidator.matches(patternUri, candidateUri));
    }

    @Test
    @DisplayName("Matches succeeds for pattern with wildcard authority and local candidate URI")
    public void testMatchesSucceedsForPatternWithWildcardAuthorityAndLocalCandidateUri() {
        UUri patternUri = UriSerializer.deserialize("//*/A410/3/1003");
        UUri candidateUri = UriSerializer.deserialize("/A410/3/1003");
        assertTrue(UriValidator.matches(patternUri, candidateUri));
    }

    @Test
    @DisplayName("Matches succeeds for pattern with wildcard entity ID")
    public void testMatchesSucceedsForPatternWithWildcardEntityId() {
        UUri patternUri = UriSerializer.deserialize("//authority/FFFF/3/1003");
        UUri candidateUri = UriSerializer.deserialize("//authority/A410/3/1003");
        assertTrue(UriValidator.matches(patternUri, candidateUri));
    }

    @Test
    @DisplayName("Matches succeeds for pattern with similar entity instance")
    public void testMatchesSucceedsForPatternWithSimilarEntityInstance() {
        UUri patternUri = UriSerializer.deserialize("//authority/A410/3/1003");
        UUri candidateUri = UriSerializer.deserialize("//authority/2A410/3/1003");
        assertTrue(UriValidator.matches(patternUri, candidateUri));
    }

    @Test
    @DisplayName("Matches succeeds for pattern with identical entity instance")
    public void testMatchesSucceedsForPatternWithIdenticalEntityInstance() {
        UUri patternUri = UriSerializer.deserialize("//authority/2A410/3/1003");
        UUri candidateUri = UriSerializer.deserialize("//authority/2A410/3/1003");
        assertTrue(UriValidator.matches(patternUri, candidateUri));
    }

    @Test
    @DisplayName("Matches succeeds for pattern with wildcard entity version")
    public void testMatchesSucceedsForPatternWithWildcardEntityVersion() {
        UUri patternUri = UriSerializer.deserialize("//authority/A410/FF/1003");
        UUri candidateUri = UriSerializer.deserialize("//authority/A410/3/1003");
        assertTrue(UriValidator.matches(patternUri, candidateUri));
    }

    @Test
    @DisplayName("Matches succeeds for pattern with wildcard resource")
    public void testMatchesSucceedsForPatternWithWildcardResource() {
        UUri patternUri = UriSerializer.deserialize("//authority/A410/3/FFFF");
        UUri candidateUri = UriSerializer.deserialize("//authority/A410/3/1003");
        assertTrue(UriValidator.matches(patternUri, candidateUri));
    }

    @Test
    @DisplayName("Matches fails for upper case authority")
    public void testMatchesFailForUpperCaseAuthority() {
        UUri pattern = UriSerializer.deserialize("//Authority/A410/3/1003");
        UUri candidate = UriSerializer.deserialize("//authority/A410/3/1003");
        assertFalse(UriValidator.matches(pattern, candidate));
    }

    @Test
    @DisplayName("Matches fails for local pattern with authority")
    public void testMatchesFailForLocalPatternWithAuthority() {
        UUri pattern = UriSerializer.deserialize("/A410/3/1003");
        UUri candidate = UriSerializer.deserialize("//authority/A410/3/1003");
        assertFalse(UriValidator.matches(pattern, candidate));
    }

    @Test
    @DisplayName("Matches fails for different authority")
    public void testMatchesFailForDifferentAuthority() {
        UUri pattern = UriSerializer.deserialize("//other/A410/3/1003");
        UUri candidate = UriSerializer.deserialize("//authority/A410/3/1003");
        assertFalse(UriValidator.matches(pattern, candidate));
    }

    @Test
    @DisplayName("Matches fails for different entity ID")
    public void testMatchesFailForDifferentEntityId() {
        UUri pattern = UriSerializer.deserialize("//authority/45/3/1003");
        UUri candidate = UriSerializer.deserialize("//authority/A410/3/1003");
        assertFalse(UriValidator.matches(pattern, candidate));
    }

    @Test
    @DisplayName("Matches fails for different entity instance")
    public void testMatchesFailForDifferentEntityInstance() {
        UUri pattern = UriSerializer.deserialize("//authority/30A410/3/1003");
        UUri candidate = UriSerializer.deserialize("//authority/2A410/3/1003");
        assertFalse(UriValidator.matches(pattern, candidate));
    }

    @Test
    @DisplayName("Matches fails for different entity version")
    public void testMatchesFailForDifferentEntityVersion() {
        UUri pattern = UriSerializer.deserialize("//authority/A410/1/1003");
        UUri candidate = UriSerializer.deserialize("//authority/A410/3/1003");
        assertFalse(UriValidator.matches(pattern, candidate));
    }

    @Test
    @DisplayName("Matches fails for different resource")
    public void testMatchesFailForDifferentResource() {
        UUri pattern = UriSerializer.deserialize("//authority/A410/3/ABCD");
        UUri candidate = UriSerializer.deserialize("//authority/A410/3/1003");
        assertFalse(UriValidator.matches(pattern, candidate));
    }

    @Test
    @DisplayName("hasWildcard() for an null UUri")
    void testHaswildcardForNullUuri() {
        assertFalse(UriValidator.hasWildcard(null));
    }

    @Test
    @DisplayName("hasWildcard() for a UUri with empty URI")
    void testHaswildcardForEmptyUuri() {
        assertFalse(UriValidator.hasWildcard(UUri.getDefaultInstance()));
    }

    @Test
    @DisplayName("hasWildcard() for a UUri with wildcard authority")
    void testHaswildcardForUuriWithWildcardAuthority() {
        UUri uri = UriSerializer.deserialize("//*/A410/3/1003");
        assertTrue(UriValidator.hasWildcard(uri));
    }

    @Test
    @DisplayName("hasWildcard() for a UUri with wildcard entity ID")
    void testHaswildcardForUuriWithWildcardEntityId() {
        UUri uri = UriSerializer.deserialize("//authority/FFFF/3/1003");
        assertTrue(UriValidator.hasWildcard(uri));
    }

    @Test
    @DisplayName("hasWildcard() for a UUri with wildcard entity version")
    void testHaswildcardForUuriWithWildcardEntityInstance() {
        UUri uri = UriSerializer.deserialize("//authority/A410/FF/1003");
        assertTrue(UriValidator.hasWildcard(uri));
    }

    @Test
    @DisplayName("hasWildcard() for a UUri with wildcard resource")
    void testHaswildcardForUuriWithWildcardResource() {
        UUri uri = UriSerializer.deserialize("//authority/A410/3/FFFF");
        assertTrue(UriValidator.hasWildcard(uri));
    }

    @Test
    @DisplayName("hasWildcard() for a UUri with no wildcards")
    void testHaswildcardForUuriWithNoWildcards() {
        UUri uri = UriSerializer.deserialize("//authority/A410/3/1003");
        assertFalse(UriValidator.hasWildcard(uri));
    }
}
