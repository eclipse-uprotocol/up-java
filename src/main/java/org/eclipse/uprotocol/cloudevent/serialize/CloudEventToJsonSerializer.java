/*
 * Copyright (c) 2023 General Motors GTO LLC
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * SPDX-FileType: SOURCE
 * SPDX-FileCopyrightText: 2023 General Motors GTO LLC
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.uprotocol.cloudevent.serialize;

import io.cloudevents.v1.proto.CloudEvent;
import com.google.protobuf.util.JsonFormat;
import com.google.protobuf.util.JsonFormat.Parser;
import com.google.protobuf.util.JsonFormat.Printer;

import java.util.Arrays;

/**
 * CloudEventSerializer to serialize and deserialize CloudEvents to JSON format.
 */
public class CloudEventToJsonSerializer implements CloudEventSerializer {

    // Force database64 encoding as we know the data will be in a protobuf format
    private static final Printer printer = JsonFormat.printer().usingTypeRegistry(JsonFormat.TypeRegistry.newBuilder().add(CloudEvent.getDescriptor()).build()).preservingProtoFieldNames().omittingInsignificantWhitespace();
    private static final Parser parser = JsonFormat.parser().usingTypeRegistry(JsonFormat.TypeRegistry.newBuilder().add(CloudEvent.getDescriptor()).build());

    public byte[] serialize(CloudEvent cloudEvent) {
        byte[] bytes;
        try {
            bytes = printer.print(cloudEvent).getBytes();
        } catch (Exception e) {
            bytes = new byte[0];
            // log an error
        }
        return bytes;
    }

    @Override
    public CloudEvent deserialize(byte[] bytes) {
        CloudEvent.Builder builder = CloudEvent.newBuilder();
        try {
            parser.merge(Arrays.toString(bytes), builder);
        } catch (Exception e) {
            // log an error
        }
        return builder.build();
    }

}
