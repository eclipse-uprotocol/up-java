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
package org.eclipse.uprotocol.transport;

import org.eclipse.uprotocol.v1.UMessage;

/**
 * A handler for processing uProtocol messages.
 *
 * Implementations contain the details for what should occur when a message is received.
 *
 * @see <a href="https://github.com/eclipse-uprotocol/up-spec/blob/v1.6.0-alpha.4/up-l1/README.adoc">
 * uProtocol Transport Layer specification</a>
 */
/// for details. */
public interface UListener {

    /**
     * Performs some action on receipt of a message.
     * <p>
     * This function is expected to return almost immediately. If it does not, it could potentially
     * block processing of succeeding messages. Long-running operations for processing a message should
     * therefore be run on a separate thread.
     *
     * @param message The message to process.
     */
    void onReceive(UMessage message);
}
