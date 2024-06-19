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
package org.eclipse.uprotocol.communication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.uprotocol.v1.UUri;
import org.eclipse.uprotocol.v1.UPriority;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


public class CallOptionsTest {
    
    @Test
    @DisplayName("Test building a null CallOptions that is equal to the default")
    public void testBuildNullCallOptions() {
        CallOptions options = new CallOptions();
        assertTrue(options.equals(CallOptions.DEFAULT));
    }

    @Test
    @DisplayName("Test building a CallOptions with a timeout")
    public void testBuildCallOptionsWithTimeout() {
        CallOptions options = new CallOptions(1000);
        assertEquals(1000, options.timeout());
        assertEquals(UPriority.UPRIORITY_CS4, options.priority());
        assertTrue(options.token().isEmpty());
    }

    @Test
    @DisplayName("Test building a CallOptions with a priority")
    public void testBuildCallOptionsWithPriority() {
        CallOptions options = new CallOptions(1000, UPriority.UPRIORITY_CS4);
        assertEquals(UPriority.UPRIORITY_CS4, options.priority());
    }


    @Test
    @DisplayName("Test building a CallOptions with all parameters")
    public void testBuildCallOptionsWithAllParameters() {
        CallOptions options = new CallOptions(1000, UPriority.UPRIORITY_CS4, "token");
        assertEquals(1000, options.timeout());
        assertEquals(UPriority.UPRIORITY_CS4, options.priority());
        assertEquals("token", options.token() );
    }

    @Test
    @DisplayName("Test building a CallOptions with a blank token")
    public void testBuildCallOptionsWithBlankToken() {
        CallOptions options = new CallOptions(1000, UPriority.UPRIORITY_CS4, "");
        assertTrue(options.token().isEmpty());
    }

    @Test
    @DisplayName("Test isEquals when passed parameter is not equals")
    public void testIsEqualsWithNull() {
        CallOptions options = new CallOptions(1000, UPriority.UPRIORITY_CS4, "token");
        assertFalse(options.equals(null));
    }

    @Test
    @DisplayName("Test isEquals when passed parameter is equals")
    public void testIsEqualsWithSameObject() {
        CallOptions options = new CallOptions(1000, UPriority.UPRIORITY_CS4, "token");
        assertTrue(options.equals(options));
    }

    @Test
    @DisplayName("Test isEquals when timeout is not the same")
    public void testIsEqualsWithDifferentParameters() {
        CallOptions options = new CallOptions(1001, UPriority.UPRIORITY_CS3, "token");
        CallOptions otherOptions = new CallOptions(1000, UPriority.UPRIORITY_CS3, "token");
        assertFalse(options.equals(otherOptions));
      
    }

    @Test
    @DisplayName("Test isEquals when priority is not the same")
    public void testIsEqualsWithDifferentParametersPriority() {
        CallOptions options = new CallOptions(1000, UPriority.UPRIORITY_CS4, "token");
        CallOptions otherOptions = new CallOptions(1000, UPriority.UPRIORITY_CS3, "token");
        assertFalse(options.equals(otherOptions));
      
    }

    @Test
    @DisplayName("Test isEquals when token is not the same")
    public void testIsEqualsWithDifferentParametersToken() {
        CallOptions options = new CallOptions(1000, UPriority.UPRIORITY_CS3, "Mytoken");
        CallOptions otherOptions = new CallOptions(1000, UPriority.UPRIORITY_CS3, "token");
        assertFalse(options.equals(otherOptions));
      
    }

    @Test
    @DisplayName("Test equals when object passed is not the same type as CallOptions")
    public void testIsEqualsWithDifferentType() {
        CallOptions options = new CallOptions(1000, UPriority.UPRIORITY_CS4, "token");
        UUri uri = UUri.getDefaultInstance();
        assertFalse(options.equals(uri));
    }
}
