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
/*
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.eclipse.uprotocol.v1.UCode;
import org.eclipse.uprotocol.v1.UMessage;
import org.eclipse.uprotocol.v1.UStatus;
import org.eclipse.uprotocol.v1.UUri;
*/
public class RpcServerTest {
/*
    @Test
    @DisplayName("Test registering a request listener")
    public void test_registering_request_listener() {

        TestUTransport transport = new TestUTransport();
        RequestListener listener = new RequestListener(transport) {
            @Override
            UPayload handleRequest(UMessage request) {
                return UPayload.EMPTY;
            }
        };
        RpcServer server = new DefaultRpcServer();
        assertEquals(server.init(transport), UStatus.newBuilder().setCode(UCode.OK).build());
        UStatus status = server.registerRequestListener(createMethodUri(), listener);
        assertEquals(status.getCode(), UCode.OK);
    }

    @Test
    @DisplayName("Test unregistering a request listener")
    public void test_unregistering_request_listener() {

        TestUTransport transport = new TestUTransport();
        RequestListener listener = new RequestListener(transport) {
            @Override
            UPayload handleRequest(UMessage request) {
                return UPayload.packToAny(UUri.newBuilder().build());
            }
        };
        RpcServer server = new DefaultRpcServer();
        assertEquals(server.init(transport), UStatus.newBuilder().setCode(UCode.OK).build());
        UStatus status = server.registerRequestListener(createMethodUri(), listener);
        assertEquals(status.getCode(), UCode.OK);

        status = server.unregisterRequestListener(createMethodUri(), listener);
        assertEquals(status.getCode(), UCode.OK);
    }

    
    @Test
    @DisplayName("Test unregistering a listener that wasn't registered before")
    public void test_unregistering_non_registered_listener() {

        TestUTransport transport = new TestUTransport();
        RequestListener listener = new RequestListener(transport) {
            @Override
            UPayload handleRequest(UMessage request) {
                throw new UnsupportedOperationException("Unimplemented method 'handleRequest'");
            }
        };
        RpcServer server = new DefaultRpcServer();
        assertEquals(server.init(transport), UStatus.newBuilder().setCode(UCode.OK).build());
        UStatus status = server.unregisterRequestListener(createMethodUri(), listener);
        assertEquals(status.getCode(), UCode.INVALID_ARGUMENT);
    }

    private UUri createMethodUri() {
        return UUri.newBuilder()
            .setAuthorityName("hartley")
            .setUeId(10)
            .setUeVersionMajor(1)
            .setResourceId(3).build();
    }
    */
}
