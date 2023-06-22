package org.eclipse.uprotocol.umessage;

public class EmptyUMessage implements UMessage {

    @Override
    public UPayload payload() {
        return UPayload.empty();
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

}
