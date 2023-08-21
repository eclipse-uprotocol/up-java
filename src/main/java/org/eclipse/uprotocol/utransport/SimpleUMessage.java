package com.eclipse.uprotocol.uhello.akkaapp.sdk.umessage;

import java.util.Objects;
import java.util.UUID;

/**
 * The most basic representation of a UMessage.
 */
public class SimpleUMessage implements UMessage {

    private static final SimpleUMessage EMPTY = new SimpleUMessage(null, UTopic.empty(), UPayload.empty(), 0);

    private final String Id;
    private final UTopic source;
    private final UPayload payload;
    private final int priority;

    /**
     * Create a simple UMessage.
     * @param id Unique identifier of the requestRpcMessage.
     * @param source The definition of the software where this requestRpcMessage originated, or the requester of an RPC.
     * @param payload The actual raw bytes or serialized bytes containing the payload of the requestRpcMessage.
     * @param priority some priority...?
     */
    public SimpleUMessage(String id, UTopic source, UPayload payload, int priority) {
        Id = Objects.requireNonNullElseGet(id, () -> UUID.randomUUID().toString());
        this.source = Objects.requireNonNullElseGet(source, UTopic::empty);
        this.payload = Objects.requireNonNullElseGet(payload, UPayload::empty);
        this.priority = priority;
    }

    public SimpleUMessage(String id, UTopic source, UPayload payload) {
        this(id, source, payload, 0);
    }

    public SimpleUMessage(UTopic source, UPayload payload) {
        this(null, source, payload, 0);
    }

    public static SimpleUMessage empty() {
        return EMPTY;
    }

    @Override
    public String id() {
        return this.Id;
    }

    @Override
    public UTopic source() {
        return this.source;
    }

    @Override
    public UPayload payload() {
        return this.payload;
    }

    @Override
    public boolean isExpired() {
        // since there is no ttl, this requestRpcMessage never expires.
        return false;
    }

    @Override
    public boolean isEmpty() {
        return this.source.isEmpty() && this.payload.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleUMessage that = (SimpleUMessage) o;
        return priority == that.priority && Objects.equals(Id, that.Id)
                && Objects.equals(source, that.source) && Objects.equals(payload, that.payload);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Id, source, payload, priority);
    }

    @Override
    public String toString() {
        return "SimpleUMessage{" +
                "Id='" + Id + '\'' +
                ", source=" + source +
                ", payload=" + payload +
                ", priority=" + priority +
                '}';
    }

    public String toLog() {
        return String.format("Id=%s, source=%s, priority=%s",
                id(), source().toLog(), priority);
    }
}
