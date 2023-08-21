package org.eclipse.uprotocol.utransport;


import java.util.Objects;

/**
 * An implementation of a UMessage mapping from a CloudEvent.
 */
public class CloudEventUMessage extends SimpleUMessage implements UMessage {

    private final static Logger logger = LoggerFactory.getLogger(CloudEventUMessage.class);

    private final CloudEvent cloudEvent;

    private CloudEventUMessage(String id, UTopic source, UPayload payload, int priority, CloudEvent cloudEvent) {
        super(id, source, payload, priority);
        this.cloudEvent = cloudEvent;
    }

    public static CloudEventUMessage fromCloudEvent(CloudEvent cloudEvent) {
        logger.info("ce [{}]", cloudEvent);
        CloudEvent ce;
        if (cloudEvent == null) {
            logger.info("ce is null");
            ce = CloudEventFactory.publish("ultifi:/", Any.getDefaultInstance(), UCloudEventAttributes.empty());
        } else {
            ce = cloudEvent;
        }
        final UltifiUri ultifiUri = UltifiUriFactory.parseFromUri(UCloudEvent.getSource(ce));
        final Any cePayload = UCloudEvent.getPayload(ce);
        final String cePriority = UCloudEvent.getPriority(ce)
                .orElseGet(UCloudEventAttributes.Priority.LOW::qosString);
        return new CloudEventUMessage(ce.getId(),
                new UTopic(ultifiUri.uAuthority(), ultifiUri.uEntity(), ultifiUri.uResource()),
                UPayload.fromProtoBytesofTypeAny(cePayload.toByteArray()),
                calculatePriority(cePriority),
                ce);
    }

    public static int calculatePriority(String qosString) {
        return Integer.parseInt(qosString.replace("CS", ""));
    }

    public static CloudEventUMessage empty() {
        return fromCloudEvent(null);
    }

    public CloudEvent cloudEvent() {
        return this.cloudEvent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        CloudEventUMessage that = (CloudEventUMessage) o;
        return Objects.equals(cloudEvent, that.cloudEvent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), cloudEvent);
    }

    @Override
    public String toString() {
        return "CloudEventUMessage{" +
                "cloudEvent=" + cloudEvent +
                "} " + super.toString();
    }
}
