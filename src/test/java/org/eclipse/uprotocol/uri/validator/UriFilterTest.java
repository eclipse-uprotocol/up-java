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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.eclipse.uprotocol.v1.UAttributes;
import org.eclipse.uprotocol.v1.UUri;


public class UriFilterTest {
    private static final UUri SOURCE_URI = UUri.newBuilder().setAuthorityName("source").build();
    private static final UUri SINK_URI = UUri.newBuilder().setAuthorityName("sink").build();
    private static final UUri OTHER_URI = UUri.newBuilder().setAuthorityName("other").build();


    @Test
    @DisplayName("Test constructor with null source and sink URIs")
    public void testConstructorWithNullSourceAndSinkURIs() {
        assertThrows(NullPointerException.class, () -> new UriFilter(null, null));
    }

    @Test
    @DisplayName("Test constructor with null source URI")
    public void testConstructorWithNullSourceURI() {
        assertThrows(NullPointerException.class, () -> new UriFilter(null, UUri.getDefaultInstance()));
    }

    @Test
    @DisplayName("Test constructor with null sink URI")
    public void testConstructorWithNullSinkURI() {
        assertThrows(NullPointerException.class, () -> new UriFilter(UUri.getDefaultInstance(), null));
    }

    @Test
    @DisplayName("Test matches with null attributes")
    public void testMatchesWithNullAttributes() {
        UriFilter uriFilter = new UriFilter(UUri.getDefaultInstance(), UUri.getDefaultInstance());
        assertFalse(uriFilter.matches(null));
    }

    @Test
    @DisplayName("Test matches with empty source and sink UUri and empty UAttributes")
    public void testMatchesWithEmptySourceAndSinkUUriAndEmptyUAttributes() {
        UriFilter uriFilter = new UriFilter(UUri.getDefaultInstance(), UUri.getDefaultInstance());
        assertTrue(uriFilter.matches(UAttributes.getDefaultInstance()));
    }

    @Test
    @DisplayName("Test matches with empty source and sink UUri and non-empty UAttributes")
    public void testMatchesWithEmptySourceAndSinkUUriAndNonEmptyUAttributes() {
        UriFilter uriFilter = new UriFilter(UUri.getDefaultInstance(), UUri.getDefaultInstance());
        assertFalse(uriFilter.matches(UAttributes.newBuilder().setSource(SOURCE_URI).setSink(SOURCE_URI).build()));
    }

    @Test
    @DisplayName("Test matches with non-empty source and sink UUri and empty UAttributes")
    public void testMatchesWithNonEmptySourceAndSinkUUriAndEmptyUAttributes() {
        UriFilter uriFilter = new UriFilter(SOURCE_URI, SINK_URI);
        assertFalse(uriFilter.matches(UAttributes.getDefaultInstance()));
    }

    @Test
    @DisplayName("Test matches with non-empty source and sink UUri and non-matching UAttributes")
    public void testMatchesWithNonEmptySourceAndSinkUUriAndNonMatchingUAttributes() {
        UriFilter uriFilter = new UriFilter(SOURCE_URI, SINK_URI);
        assertFalse(uriFilter.matches(UAttributes.newBuilder().setSource(OTHER_URI).setSink(OTHER_URI).build()));
    }

    @Test
    @DisplayName("Test matches with non-empty source and sink UUri and matching UAttributes")
    public void testMatchesWithNonEmptySourceAndSinkUUriAndMatchingUAttributes() {
        UriFilter uriFilter = new UriFilter(SOURCE_URI, SINK_URI);
        assertTrue(uriFilter.matches(UAttributes.newBuilder().setSource(SOURCE_URI).setSink(SINK_URI).build()));
    }

    @Test
    @DisplayName("Test matches source and sink UUri and matching source and non-matching sink UAttributes")
    public void testMatchesWithNonEmptySourceAndSinkUUriAndMatchingSourceAndNonMatchingSinkUAttributes() {
        UriFilter uriFilter = new UriFilter(SOURCE_URI, SINK_URI);
        assertFalse(uriFilter.matches(UAttributes.newBuilder().setSource(SOURCE_URI).setSink(OTHER_URI).build()));
    }

    @Test
    @DisplayName("Test matches source and sink UUri and non-matching source and matching sink UAttributes")
    public void testMatchesWithNonEmptySourceAndSinkUUriAndNonMatchingSourceAndMatchingSinkUAttributes() {
        UriFilter uriFilter = new UriFilter(SOURCE_URI, SINK_URI);
        assertFalse(uriFilter.matches(UAttributes.newBuilder().setSource(OTHER_URI).setSink(SINK_URI).build()));
    }

    @Test
    @DisplayName("Test fetching the source and sink and verifying they match what was passed to the constructor")
    public void testFetchingSourceAndSink() {
        UriFilter uriFilter = new UriFilter(SOURCE_URI, SINK_URI);
        assertTrue(SOURCE_URI.equals(uriFilter.source()));
        assertTrue(SINK_URI.equals(uriFilter.sink()));
    }
    
}
