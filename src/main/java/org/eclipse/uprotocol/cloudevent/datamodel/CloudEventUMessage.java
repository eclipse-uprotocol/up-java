package org.eclipse.uprotocol.cloudevent.datamodel;

import java.util.Objects;

import org.eclipse.uprotocol.cloudevent.factory.UCloudEvent;
import org.eclipse.uprotocol.transport.datamodel.UAttributes;
import org.eclipse.uprotocol.transport.datamodel.UMessage;
import org.eclipse.uprotocol.transport.datamodel.UPayload;
import org.eclipse.uprotocol.transport.datamodel.USerializationHint;
import org.eclipse.uprotocol.uri.datamodel.UUri;
import org.eclipse.uprotocol.uri.serializer.LongUriSerializer;
import io.cloudevents.CloudEvent;

public class CloudEventUMessage implements UMessage {

    private final CloudEvent ce;

    public CloudEventUMessage(CloudEvent ce) {
        Objects.requireNonNull(ce, "null CloudEvent");
        this.ce = ce;
    }
    
    @Override
    public UUri topic() {
        return LongUriSerializer.instance().deserialize(UCloudEvent.getSource(ce));
    }

    @Override
    public UPayload payload() {
        return new UPayload(UCloudEvent.getPayload(ce).toByteArray(), USerializationHint.PROTOBUF);
    }

    @Override
    public UAttributes attributes() {
        return UAttributes.empty();
    }
    
}
