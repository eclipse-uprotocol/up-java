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
 */

package org.eclipse.uprotocol.transport.factory;

import com.google.rpc.Code;
import org.eclipse.uprotocol.transport.builder.UAttributesBuilder;
import org.eclipse.uprotocol.v1.UMessageType;
import org.eclipse.uprotocol.v1.UPriority;
import org.eclipse.uprotocol.v1.UUID;
import org.eclipse.uprotocol.v1.UUri;

public interface UAttributesBuilderFactory {

    /**
     * Create an UAttributesBuilder for an event for the use case of: RPC Request message.
     *
     * @param id   UUID object
     * @param sink The uri for the method to be called on the service Ex.: :/body.access/1/rpc.UpdateDoor
     * @return Returns a request UAttributesBuilder.
     */

    static UAttributesBuilder request(UUID id, UUri sink) {
        return new UAttributesBuilder(id, UMessageType.REQUEST, UPriority.CS4).withSink(sink);
    }

    /**
     * Create an UAttributesBuilder for an event for the use case of: RPC Response message.
     *
     * @param id        UUID object
     * @param sink      The destination of the response. The uri for the original application that requested the RPC
     *                  and this response is for.
     * @param requestId The uuid from the original request that this response is for.
     * @return Returns a response UAttributes.
     */
    static UAttributesBuilder response(UUID id, UUri sink, UUID requestId) {
        return new UAttributesBuilder(id, UMessageType.RESPONSE, UPriority.CS4).withSink(sink).withReqId(requestId);
    }

    /**
     * Create an UAttributesBuilder for an event for the use case of: RPC Response message that failed.
     *
     * @param id                  UUID object
     * @param sink                The destination of the response. The uri for the original application that
     *                            requested the RPC
     *                            and this response is for.
     * @param requestId           The uuid from the original request that this response is for.
     * @param communicationStatus A {@link Code} value that indicates of a platform communication error while
     *                            delivering this CloudEvent.
     * @return Returns a response UAttributesBuilder.
     */
    static UAttributesBuilder failedResponse(UUID id, UUri sink, UUID requestId, Integer communicationStatus) {
        return new UAttributesBuilder(id, UMessageType.RESPONSE, UPriority.CS4).withSink(sink).withReqId(requestId)
                .withCommStatus(communicationStatus);
    }

    /**
     * Create an UAttributesBuilder for an event for the use case of: Publish generic message.
     *
     * @param id UUID object
     * @return Returns a request UAttributesBuilder.
     */

    static UAttributesBuilder publish(UUID id) {
        return new UAttributesBuilder(id, UMessageType.PUBLISH, UPriority.CS1);
    }

    /**
     * Create an UAttributesBuilder for an event for the use case of: RPC Request message.
     *
     * @param id   UUID object
     * @param sink The  uri of the destination of this notification.
     * @return Returns a request UAttributesBuilder.
     */
    static UAttributesBuilder notification(UUID id, UUri sink) {
        return new UAttributesBuilder(id, UMessageType.PUBLISH, UPriority.CS1).withSink(sink);
    }
}
