package org.eclipse.uprotocol.utransport;

import java.util.Objects;
import java.util.UUID;

/**
 * Class for returning new Empty Messages. This can be useful to return empty values in cases where there are
 * errors. The requestRpcMessage still has a unique identifier for tracing but is empty since there is nothing to add since there was an error.
 * TODO check if this is needed, or use a simple requestRpcMessage with empty
 */
public class EmptyUMessage implements UMessage {

    private final String id;

    /**
     * Construct an EmptyUMessage.
     * @param id Unique identifier of the requestRpcMessage.
     */
    public EmptyUMessage(String id) {
        this.id = id;
    }

    public EmptyUMessage() {
        this(UUID.randomUUID().toString());
    }

    @Override
    public String id() {
        return this.id;
    }

    @Override
    public UTopic source() {
        return UTopic.empty();
    }

    @Override
    public UPayload payload() {
        return UPayload.empty();
    }

    @Override
    public boolean isExpired() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public String toLog() {
        return toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmptyUMessage that = (EmptyUMessage) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "EmptyUMessage{" +
                "id='" + id + '\'' +
                '}';
    }
}
