   
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
import org.eclipse.uprotocol.v1.UCode;
import org.eclipse.uprotocol.v1.UStatus;
import org.eclipse.uprotocol.v1.UUri;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


public class DefaultPublisherTest {
    @Test
    @DisplayName("Test sending a simple publish message without a payload")
    public void testSendPublish() {
        Publisher publisher = new DefaultPublisher(new TestUTransport());
        UStatus status = publisher.publish(createTopic(), null);
        assertEquals(status.getCode(), UCode.OK);
    }
    
    @Test
    @DisplayName("Test sending a simple publish message with a stuffed UPayload that was build with packToAny()")
    public void testSendPublishWithStuffedPayload() {
        UUri uri = UUri.newBuilder().setAuthorityName("Hartley").build();
        Publisher publisher = new DefaultPublisher(new TestUTransport());
        UStatus status = publisher.publish(createTopic(), UPayload.packToAny(uri));
        assertEquals(status.getCode(), UCode.OK);
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

