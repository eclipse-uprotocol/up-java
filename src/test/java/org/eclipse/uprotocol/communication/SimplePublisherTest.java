   
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

import java.util.concurrent.CompletionStage;

import org.eclipse.uprotocol.v1.UCode;
import org.eclipse.uprotocol.v1.UStatus;
import org.eclipse.uprotocol.v1.UUri;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


public class SimplePublisherTest {
    @Test
    @DisplayName("Test sending a simple publish message without a payload")
    public void testSendPublish() {
        Publisher publisher = new SimplePublisher(new TestUTransport());
        CompletionStage<UStatus> result = publisher.publish(createTopic());
        assertEquals(result.toCompletableFuture().join().getCode(), UCode.OK);
    }

    @Test
    @DisplayName("Test sending a simple publish message with CallOptions and no payload")
    public void testSendPublishWithOptions() {
        Publisher publisher = new SimplePublisher(new TestUTransport());
        CompletionStage<UStatus> result = publisher.publish(createTopic(), CallOptions.DEFAULT);
        assertEquals(result.toCompletableFuture().join().getCode(), UCode.OK);
    }
    
    @Test
    @DisplayName("Test sending a simple publish message with a stuffed UPayload that was build with packToAny()")
    public void testSendPublishWithStuffedPayload() {
        UUri uri = UUri.newBuilder().setAuthorityName("Hartley").build();
        Publisher publisher = new SimplePublisher(new TestUTransport());
        CompletionStage<UStatus> result = publisher.publish(createTopic(), UPayload.packToAny(uri));
        assertEquals(result.toCompletableFuture().join().getCode(), UCode.OK);
    }

    @Test
    @DisplayName("Test sending a simple publish message with CallOptions and a stuffed UPayload")
    public void testSendPublishWithPayloadAndOptions() {
        UUri uri = UUri.newBuilder().setAuthorityName("Hartley").build();
        Publisher publisher = new SimplePublisher(new TestUTransport());
        CompletionStage<UStatus> result = publisher.publish(createTopic(), CallOptions.DEFAULT, UPayload.pack(uri));
        assertEquals(result.toCompletableFuture().join().getCode(), UCode.OK);
    }
   
    private UUri createTopic() {
        return UUri.newBuilder()
            .setAuthorityName("hartley")
            .setUeId(3)
            .setUeVersionMajor(1)
            .setResourceId(0x8000)
            .build();
    }
}

