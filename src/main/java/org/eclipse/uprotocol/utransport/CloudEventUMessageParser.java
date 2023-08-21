package org.eclipse.uprotocol.utransport;

import java.net.URI;
import java.util.Optional;

/**
 * Parsing to and from the CloudEvent PDU - protocol data unit, a.k.a the requestRpcMessage envelope/object that contains the data & metadata.
 * //TODO need to figure out attributes and other aspects of building the correct CloudEvent
 * //TODO how to handle errors? I don't like exceptions very much. Sending something invalid over the wire is yucky as well.
 */
public record CloudEventUMessageParser() {

    private static final Logger logger = LoggerFactory.getLogger(CloudEventUMessageParser.class);
    private static final String NONE = "NONE";

    public static UMessage toUMessage(CloudEvent cloudEvent) {
        logger.info("processing CloudEvent [{}]", cloudEvent);
        if (cloudEvent == null) {
            logger.info("CloudEvent is null");
            return SimpleUMessage.empty();
        }
        final UltifiUri cloudEventsourceUri = UltifiUriFactory.parseFromUri(UCloudEvent.getSource(cloudEvent));
        final Any cePayload = UCloudEvent.getPayload(cloudEvent);
        final String cloudEventType = cloudEvent.getType();
        final Optional<String> maybeSink = UCloudEvent.getSink(cloudEvent);
        if (UCloudEventType.REQUEST.type().equals(cloudEventType)) {
            if (maybeSink.isPresent()) {
                UltifiUri sink = UltifiUriFactory.parseFromUri(maybeSink.get());
                return RpcUMessage.buildRpcRequest(cloudEvent.getId(),
                        new UTopic(cloudEventsourceUri.uAuthority(), cloudEventsourceUri.uEntity(), cloudEventsourceUri.uResource()),
                        new UTopic(sink.uAuthority(), sink.uEntity(), sink.uResource()),
                        UPayload.fromProtoBytesofTypeAny(cePayload.toByteArray()));
            }
        } else if (UCloudEventType.RESPONSE.type().equals(cloudEventType)) {
            if (maybeSink.isPresent()) {
                UltifiUri sink = UltifiUriFactory.parseFromUri(maybeSink.get());
                return RpcUMessage.buildRpcResponse(cloudEvent.getId(),
                        new UTopic(sink.uAuthority(), sink.uEntity(), sink.uResource()),
                        new UTopic(cloudEventsourceUri.uAuthority(), cloudEventsourceUri.uEntity(), cloudEventsourceUri.uResource()),
                        UCloudEvent.getRequestId(cloudEvent).orElse(NONE),
                        UPayload.fromProtoBytesofTypeAny(cePayload.toByteArray()));
            }
        }
        return CloudEventUMessage.fromCloudEvent(cloudEvent);
    }

    public static CloudEvent toCloudEvent(UMessage uMessage) {
        logger.info("building CloudEvent from [{}]", uMessage);
        final UTopic sourceTopic = uMessage.source();
        String uMessageSource = UltifiUriFactory.buildUProtocolUri(sourceTopic.uAuthority(),
                sourceTopic.uEntity(), sourceTopic.uResource());
        Any protoPayload = getProtoPayloadFromMessage(uMessage);

        if (uMessage instanceof RpcUMessage rpcUMessage) {
            final boolean isRequest = rpcUMessage.isRPCRequest();
            final UTopic sinkTopic = rpcUMessage.sink();
            String serviceMethodForUri = UltifiUriFactory.buildUProtocolUri(sinkTopic.uAuthority(),
                    sinkTopic.uEntity(), sinkTopic.uResource());
            if (isRequest) {
                return CloudEventFactory.buildBaseCloudEvent(rpcUMessage.id(), uMessageSource,
                                protoPayload.toByteArray(), protoPayload.getTypeUrl(),
                                UCloudEventAttributes.empty())
                        .withType(UCloudEventType.REQUEST.type())
                        .withExtension("sink", serviceMethodForUri)
                        .build();
            } else {
                return CloudEventFactory.buildBaseCloudEvent(rpcUMessage.id(), serviceMethodForUri,
                                protoPayload.toByteArray(), protoPayload.getTypeUrl(),
                                UCloudEventAttributes.empty())
                        .withType(UCloudEventType.RESPONSE.type())
                        .withExtension("sink", URI.create(uMessageSource))
                        .withExtension("reqid", rpcUMessage.requestId().orElse(NONE))
                        .build();
            }
        }
        if (uMessage instanceof CloudEventUMessage cloudEventUMessage) {
            logger.info("building CloudEventUMessage");
            return cloudEventUMessage.cloudEvent();
        }
        if (uMessage instanceof SimpleUMessage) {
            return CloudEventFactory.publish(uMessageSource, protoPayload, UCloudEventAttributes.empty());
        }
        logger.error("building base cloud event since we don't know what we have");
        // we don't really know what we have, so build something
        return CloudEventFactory.buildBaseCloudEvent(
                CloudEventFactory.generateTimeOrderedCloudEventId(),
                uMessageSource,
                protoPayload.toByteArray(),
                protoPayload.getTypeUrl(),
                UCloudEventAttributes.empty())
                .build();
    }

    private static Any getProtoPayloadFromMessage(UMessage uMessage) {
        try {
            return Any.parseFrom(uMessage.payload().data());
        } catch (InvalidProtocolBufferException e) {
            logger.error("Unable to build the CloudEvent payload since we can't parse the Payload into an Any object.");
            return Any.getDefaultInstance();
            //throw new RuntimeException(e);
        }
    }

}
