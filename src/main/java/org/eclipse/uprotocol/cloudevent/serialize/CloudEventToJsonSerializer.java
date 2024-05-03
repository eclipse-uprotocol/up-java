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

import io.cloudevents.CloudEvent;
import io.cloudevents.jackson.JsonFormat;

/**
 * CloudEventSerializer to serialize and deserialize CloudEvents to JSON format.
 */
public class CloudEventToJsonSerializer implements CloudEventSerializer {

    // Force database64 encoding as we know the data will be in a protobuf format
    private static final JsonFormat serializer = new JsonFormat(true, false);

    public byte[] serialize(CloudEvent cloudEvent) {
        return serializer.serialize(cloudEvent);
    }

    @Override
    public CloudEvent deserialize(byte[] bytes) {
        return serializer.deserialize(bytes);
    }

}
