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
package org.eclipse.uprotocol.cloudevent.serialize;

/**
 * Provides Singleton instances of the CloudEvent Serializers.
 */
public enum CloudEventSerializers {
    JSON (new CloudEventToJsonSerializer()),
    PROTOBUF (new CloudEventToProtobufSerializer());

    private final CloudEventSerializer cloudEventSerializer;
    public CloudEventSerializer serializer() {
        return cloudEventSerializer;
    }

    CloudEventSerializers(CloudEventSerializer cloudEventSerializer) {
        this.cloudEventSerializer = cloudEventSerializer;
    }
}
