package com.gm.ultifi.rs.hello.app.uprotocol.utransport;

import com.azure.messaging.eventhubs.*;
import com.azure.messaging.eventhubs.models.ErrorContext;
import com.azure.messaging.eventhubs.models.EventContext;
import org.eclipse.uprotocol.Ack;
import org.eclipse.uprotocol.umessage.CloudEventUMessage;
import org.eclipse.uprotocol.umessage.EmptyUMessage;
import org.eclipse.uprotocol.umessage.UMessage;
import org.eclipse.uprotocol.cloudevent.serialize.CloudEventSerializer;
import org.eclipse.uprotocol.cloudevent.serialize.CloudEventSerializers;

import java.util.Optional;

/**
 * The EventHub Transport will transport CloudEvent implementation of UMessage using JSON serialization of the CloudEVent in the payload.
 * The content type is JSON and the message data model is EventHubContext with a CloudEvent inside.
 */
public class EventHubTransport implements UTransport<EventContext> {

    /**
     * The underlying transport technology for sending messages.
     */
    private final EventHubProducerClient eventHubProducerClient;

    /**
     * The underlying transport technology for receiving messages.
     */
    private final EventProcessorClient eventProcessorClient;

    private final UTransportMessageProcessor uMessageProcessor;

    /**
     * This is the defined content type for the uP-L1 transport.
     */
    CloudEventSerializer serializer = CloudEventSerializers.JSON.serializer();


    public EventHubTransport(EventHubProducerClient eventHubProducerClient,
                             EventProcessorClientBuilder eventProcessorClientBuilder,
                             CheckpointStore checkpointStore,
                             UTransportMessageProcessor uMessageProcessor) {
        this.uMessageProcessor = uMessageProcessor;
        this.eventHubProducerClient = eventHubProducerClient;
        this.eventProcessorClient = eventProcessorClientBuilder
                .processEvent((eventContext -> uMessageProcessor.uMessageProcessor().accept(unwrap(eventContext))))
                .processError((errorContext) -> uMessageProcessor.uMessageErrorProcessor().accept(unwrapError(errorContext)))
                .checkpointStore(checkpointStore)
                .buildEventProcessorClient();
        eventProcessorClient.start();
    }

    private UMessage unwrapError(ErrorContext errorContext) {
        return Optional.ofNullable(errorContext)
                .map(ErrorContext::getThrowable)
                .map((boom) -> new EmptyUMessage())
                .orElse(new EmptyUMessage());
    }

    /**
     * Translates a specific Transport protocol into a UMessage.
     *
     * @param eventContext Specific T transport protocol
     * @return Returns a UMessage from the T transport protocol.
     */
    @Override
    public UMessage unwrap(EventContext eventContext) {
        return Optional.ofNullable(eventContext)
                .map(EventContext::getEventData)
                .map(EventData::getBody)
                .map(rawMessageBytes -> serializer.deserialize(rawMessageBytes))
                .map(CloudEventUMessage::new)
                .orElse(new CloudEventUMessage(null));
    }

    /**
     * Translates a UMessage into a specific T Transport protocol.
     *
     * @param uMessage The UMessage to be wrapped and sent on the T Transport.
     * @return Returns the T transport protocol.
     */
    @Override
    public EventContext wrap(UMessage uMessage) {
        return null;
    }

    /**
     * Runs on the client, sends the message using the underlying transport and returns, in a fire and forget fashion.
     *
     * @param uMessage The UMessage to be sent on the underlying transport.
     * @return Returns immediately with an Ack if it could be sent.
     */
    @Override
    public Ack send(UMessage uMessage) {
        byte[] serialized = uMessage.payload().data();
        EventData eventData = new EventData(serialized);
        EventDataBatch eventDataBatch = eventHubProducerClient.createBatch();
        eventDataBatch.tryAdd(eventData);
        eventHubProducerClient.send(eventDataBatch);
        return Ack.ok();
    }

    /**
     * Support for retrieving the configured uMessage listener that will be called when a message is received by the underlying transport layer.
     *
     * @return Returns the uMessage listener that will be called when a message is received by the underlying transport layer.
     */
    @Override
    public UTransportMessageProcessor uMessageProcessor() {
        return this.uMessageProcessor;
    }

}
