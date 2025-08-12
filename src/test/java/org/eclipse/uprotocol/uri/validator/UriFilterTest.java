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
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.eclipse.uprotocol.uri.factory.UriFactory;
import org.eclipse.uprotocol.v1.UAttributes;
import org.eclipse.uprotocol.v1.UUri;


public class UriFilterTest {
    private static final UUri SOURCE_URI = UUri.newBuilder().setAuthorityName("source").build();
    private static final UUri SINK_URI = UUri.newBuilder().setAuthorityName("sink").build();
    private static final UUri OTHER_URI = UUri.newBuilder().setAuthorityName("other").build();


    @Test
    @DisplayName("Test constructor with null source and sink URIs")
    public void testConstructorWithNullSourceAndSinkURIs() {
        final UriFilter filter = new UriFilter(null, null);
        assertTrue(filter.source().equals(UriFactory.ANY));
        assertTrue(filter.sink().equals(UriFactory.ANY));
    }

    @Test
    @DisplayName("Test constructor with null source URI")
    public void testConstructorWithNullSourceURI() {
        final UriFilter filter = new UriFilter(null, SINK_URI);
        assertTrue(filter.source().equals(UriFactory.ANY));
        assertTrue(filter.sink().equals(SINK_URI));
    }

    @Test
    @DisplayName("Test constructor with null sink URI")
    public void testConstructorWithNullSinkURI() {
        final UriFilter filter = new UriFilter(SOURCE_URI, null);
        assertTrue(filter.source().equals(SOURCE_URI));
        assertTrue(filter.sink().equals(UriFactory.ANY));
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

    @Test
    @DisplayName("Test matching when source is UriFactory.ANY and sink is not UriFactory.ANY")
    public void testMatchingWhenSourceIsUriFactoryAnyAndSinkIsNotUriFactoryAny() {
        UriFilter uriFilter = new UriFilter(UriFactory.ANY, SINK_URI);
        assertTrue(uriFilter.matches(UAttributes.newBuilder().setSink(SINK_URI).build()));
    }

    @Test
    @DisplayName("Test matching when sink is UriFactory.ANY and source is not UriFactory.ANY")
    public void testMatchingWhenSinkIsUriFactoryAnyAndSourceIsNotUriFactoryAny() {
        UriFilter uriFilter = new UriFilter(SOURCE_URI, UriFactory.ANY);
        assertTrue(uriFilter.matches(UAttributes.newBuilder().setSource(SOURCE_URI).build()));
    }
    
}
