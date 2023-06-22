package org.eclipse.uprotocol.utransport;

import org.eclipse.uprotocol.umessage.UMessage;

import java.util.function.Consumer;

/**
 * Container for the functions that will be called for processing the messages that are produced by a transport.
 */
public class UTransportMessageProcessor {

    private final Consumer<UMessage> uMessageProcessor;
    private final Consumer<UMessage> uMessageErrorProcessor;

    public UTransportMessageProcessor(Consumer<UMessage> uMessageProcessor, Consumer<UMessage> uMessageErrorProcessor) {
        this.uMessageProcessor = uMessageProcessor;
        this.uMessageErrorProcessor = uMessageErrorProcessor;
    }

    public Consumer<UMessage> uMessageProcessor() {
        return this.uMessageProcessor;
    }

    public Consumer<UMessage> uMessageErrorProcessor() {
        return this.uMessageErrorProcessor;
    }
}
