package org.eclipse.uprotocol.umessage;

import io.cloudevents.CloudEvent;

public class CloudEventUMessage implements UMessage {

    private final CloudEvent cloudEvent;

    public CloudEventUMessage(CloudEvent cloudEvent) {
        this.cloudEvent = cloudEvent;
    }

    @Override
    public boolean isEmpty() {
        // this can be done because I am not exposing the CloudEvent to the outside world.
        return cloudEvent == null;
    }

    @Override
    public UPayload payload() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'payload'");
    }
}
