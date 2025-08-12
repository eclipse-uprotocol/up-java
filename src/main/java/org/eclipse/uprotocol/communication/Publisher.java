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

import java.util.concurrent.CompletionStage;

import org.eclipse.uprotocol.v1.UStatus;
import org.eclipse.uprotocol.v1.UUri;

/**
 * uP-L2 interface and data models for Java.<BR>
 * 
 * uP-L1 interfaces implements the core uProtocol across various the communication middlewares
 * and programming languages while uP-L2 API are the client-facing APIs that wrap the transport
 * functionality into easy to use, language specific, APIs to do the most common functionality
 * of the protocol (subscribe, publish, notify, invoke a Method, or handle RPC requests).
 */
public interface Publisher {
    /**
     * Publish a message to a topic.
     * 
     * @param topic The topic to publish to.
     * @return Returns the {@link UStatus} with the status of the publish.
     */
    default CompletionStage<UStatus> publish(UUri topic) {
        return publish(topic, null, null);
    }

    /**
     * Publish a message to a topic with specific {@link CallOptions}.
     * 
     * @param topic The topic to publish to.
     * @param options The {@link CallOptions} for the publish.
     * @return Returns the {@link UStatus} with the status of the publish.
     */
    default CompletionStage<UStatus> publish(UUri topic, CallOptions options) {
        return publish(topic, options, null);
    }

    /**
     * Publish a message to a topic passing {@link UPayload} as the payload.
     * 
     * @param topic The topic to publish to.
     * @param payload The {@link UPayload} to publish.
     * @return Returns the {@link UStatus} with the status of the publish.
     */
    default CompletionStage<UStatus> publish(UUri topic, UPayload payload) {
        return publish(topic, null, payload);
    }

    /**
     * Publish a message to a topic passing {@link UPayload} as the payload and with specific {@link CallOptions}.
     * 
     * @param topic The topic to publish to.
     * @param options The {@link CallOptions} for the publish.
     * @param payload The {@link UPayload} to publish.
     * @return Returns the {@link UStatus} with the status of the publish.
     */
    CompletionStage<UStatus> publish(UUri topic, CallOptions options, UPayload payload);
}
