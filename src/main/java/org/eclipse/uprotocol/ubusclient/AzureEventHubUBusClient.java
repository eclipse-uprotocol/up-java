package com.gm.ultifi.rs.hello.app.uprotocol.ubusclient;

import com.gm.ultifi.rs.hello.app.uprotocol.Ack;
import com.gm.ultifi.rs.hello.app.uprotocol.topic.Topic;
import com.gm.ultifi.rs.hello.app.uprotocol.umessage.UMessage;
import com.gm.ultifi.rs.hello.app.uprotocol.utransport.EventHubTransport;
import com.gm.ultifi.sdk.uprotocol.uri.datamodel.UEntity;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * A very specific UBusClient that has a transport layer of EventHub and a content type of {@link com.gm.ultifi.rs.hello.app.uprotocol.umessage.CloudEventUMessage}
 * serialized to JSON.
 */
public class AzureEventHubUBusClient implements UBusClient {

    private final EventHubTransport eventHubTransport;

    /**
     * Construct a new AzureEventHubClient with the underlying transport.
     * @param eventHubTransport
     */
    public AzureEventHubUBusClient(EventHubTransport eventHubTransport) {
        this.eventHubTransport = eventHubTransport;
    }

    @Override
    public Ack connect(UEntity uEntity) {
        return null;
    }

    @Override
    public CompletionStage<Ack> publish(UMessage uMessage) {
        return CompletableFuture.completedFuture(eventHubTransport.send(uMessage));
    }

    @Override
    public CompletionStage<Ack> invokeMethod(UMessage method) {
        return null;
    }

    /**
     * @param topic
     * @param subscriber
     * @return Returns an Ack indicating if the subscription was validated and accepted by UBus.
     */
    @Override
    public CompletionStage<Ack> subscribe(UTopic topic, UTopic subscriber) {
        return UBusClient.super.subscribe(topic, subscriber);
    }

    @Override
    public CompletionStage<Ack> registerListener(UTopic topic) {
        return null;
    }

    @Override
    public Object doException(Throwable throwable) {
        return null;
    }
}
