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
 * For any implementation that defines some kind of callback or function that will be called to handle incoming messages.
 */
public interface UListener {

    /**
     * Method called to handle/process messages.
     * @param message Message received.
     * @return Returns an Ack every time a message is received and processed.
     */
    void onReceive(UMessage message);
}
