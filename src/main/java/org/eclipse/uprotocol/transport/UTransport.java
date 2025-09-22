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

import java.util.Optional;
import java.util.concurrent.CompletionStage;

import org.eclipse.uprotocol.communication.UStatusException;
import org.eclipse.uprotocol.uri.factory.UriFactory;
import org.eclipse.uprotocol.v1.UAttributes;
import org.eclipse.uprotocol.v1.UMessage;
import org.eclipse.uprotocol.v1.UUri;

/**
 * UTransport is the uP-L1 interface that provides a common API for uE developers to send and receive messages.
 * <p>
 * UTransport implementations contain the details for connecting to the underlying transport technology and
 * sending UMessage using the configured technology.
 *
 * @see <a href="https://github.com/eclipse-uprotocol/up-spec/blob/v1.6.0-alpha.6/up-l1/README.adoc">
 * uProtocol Transport Layer specification</a>
 */
// [impl->dsn~utransport-declaration~1]
public interface UTransport {
    /**
     * Sends a message using this transport's message exchange mechanism.
     *
     * @param message The message to send. The <em>type</em>, <em>source</em> and <em>sink</em> properties of the
     *                {@link UAttributes} contained in the message determine the addressing semantics.
     * @return The outcome of the operation. The stage will be completed with a {@link UStatusException} if
     * the message could not be sent.
     * @throws NullPointerException if the argument is {@code null}.
     */
    CompletionStage<Void> send(UMessage message);

    /**
     * Registers a listener to be called for messages.
     * <p>
     * The listener will be invoked for each message that matches the given source filter pattern
     * according to the rules defined by the
     * <a href="https://github.com/eclipse-uprotocol/up-spec/blob/v1.6.0-alpha.6/basics/uri.adoc">UUri
     * specification</a>.
     * <p>
     * This default implementation invokes {@link #registerListener(UUri, Optional<UUri>, UListener)} with the
     * given source filter and a sink filter of {@link UriFactory#ANY}.
     *
     * @param sourceFilter The <em>source</em> address pattern that messages need to match.
     * Use {@link UriFactory#ANY} to match any source.
     * @param listener     The listener to invoke. The listener can be unregistered again
     * using {@link #unregisterListener(UUri, UListener)}.
     * @return The outcome of the operation. The stage will be completed with a {@link UStatusException} if
     * the listener could not be registered.
     * @throws NullPointerException if any of the arguments are {@code null}.
     */
    default CompletionStage<Void> registerListener(UUri sourceFilter, UListener listener) {
        return registerListener(sourceFilter, Optional.of(UriFactory.ANY), listener);
    }

    /**
     * Registers a listener to be called for messages.
     * <p>
     * The listener will be invoked for each message that matches the given source and sink filter patterns
     * according to the rules defined by the
     * <a href="https://github.com/eclipse-uprotocol/up-spec/blob/v1.6.0-alpha.6/basics/uri.adoc">UUri
     * specification</a>.
     *
     * @param sourceFilter The <em>source</em> address pattern that messages need to match.
     * Use {@link UriFactory#ANY} to match any source.
     * @param sinkFilter   The <em>sink</em> address pattern that messages need to match.
     * Use {@link UriFactory#ANY} to match any sink. Use {@link Optional#empty()} to match only messages without a sink.
     * @param listener     The listener to invoke. The listener can be unregistered again
     * using {@link #unregisterListener(UUri, Optional<UUri>, UListener)}.
     * @return The outcome of the operation. The stage will be completed with a {@link UStatusException} if
     * the listener could not be registered.
     * @throws NullPointerException if any of the arguments are {@code null}.
     */
    CompletionStage<Void> registerListener(UUri sourceFilter, Optional<UUri> sinkFilter, UListener listener);

    /**
     * Unregisters a previously {@link #registerListener(UUri, UListener) registered} message listener.
     * <p>
     * The listener will no longer be called for any (matching) messages after this method has
     * returned successfully.
     * <p>
     * This default implementation invokes {@link #unregisterListener(UUri, Optional<UUri>, UListener)} with the
     * given source filter and a sink filter of {@link UriFactory#ANY}.
     *
     * @param sourceFilter The <em>source</em> address pattern that the listener had been registered for.
     * @param listener      The listener to unregister.
     * @return The outcome of the operation. The stage will be completed with a {@link UStatusException} if
     * the listener could not be unregistered.
     * @throws NullPointerException if any of the arguments are {@code null}.
     */
    default CompletionStage<Void> unregisterListener(UUri sourceFilter, UListener listener) {
        return unregisterListener(sourceFilter, Optional.of(UriFactory.ANY), listener);
    }

    /**
     * Unregisters a previously {@link #registerListener(UUri, Optional<UUri>, UListener) registered} message listener.
     * <p>
     * The listener will no longer be called for any (matching) messages after this method has
     * returned successfully.
     *
     * @param sourceFilter The <em>source</em> address pattern that the listener had been registered for.
     * @param sinkFilter   The <em>sink</em> address pattern that the listener had been registered for.
     * @param listener      The listener to unregister.
     * @return The outcome of the operation. The stage will be completed with a {@link UStatusException} if
     * the listener could not be unregistered.
     * @throws NullPointerException if any of the arguments are {@code null}.
     */
    CompletionStage<Void> unregisterListener(UUri sourceFilter, Optional<UUri> sinkFilter, UListener listener);
}
