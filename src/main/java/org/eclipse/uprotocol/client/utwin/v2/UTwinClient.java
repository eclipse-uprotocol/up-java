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
package org.eclipse.uprotocol.client.utwin.v2;

import java.util.concurrent.CompletionStage;

import org.eclipse.uprotocol.communication.CallOptions;
import org.eclipse.uprotocol.core.utwin.v2.GetLastMessagesResponse;
import org.eclipse.uprotocol.v1.UUriBatch;

/**
 * The uTwin client-side interface.
 * 
 * UTwin is used to fetch the last published message for a given topic. This is the client-side of the 
 * UTwin Service contract and communicates with a local uTwin service to fetch the last message for a given topic.
 
 */
public interface UTwinClient {
    /**
     * Fetch the last messages for a batch of topics.
     * 
     * @param topics  {@link UUriBatch} batch of 1 or more topics to fetch the last messages for.
     * @param options The call options.
     * @return CompletionStage completes successfully with {@link GetLastMessagesResponse} if uTwin was able
     *         to fetch the topics or completes exceptionally with {@link UStatus} with the failure reason.
     *         such as {@code UCode.NOT_FOUND}, {@code UCode.PERMISSION_DENIED} etc...
     */
    CompletionStage<GetLastMessagesResponse> getLastMessages(UUriBatch topics, CallOptions options);


    /**
     * Fetch the last messages for a batch of topics.
     * 
     * @param topics  {@link UUriBatch} batch of 1 or more topics to fetch the last messages for.
     * @return CompletionStage completes successfully with {@link GetLastMessagesResponse} if uTwin was able
     *         to fetch the topics or completes exceptionally with {@link UStatus} with the failure reason.
     *         such as {@code UCode.NOT_FOUND}, {@code UCode.PERMISSION_DENIED} etc...
     */
    default CompletionStage<GetLastMessagesResponse> getLastMessages(UUriBatch topics) {
        return getLastMessages(topics, CallOptions.DEFAULT);
    }
}
