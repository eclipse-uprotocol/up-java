package org.eclipse.uprotocol.umessage;

public class UPayload {

    // TODO fix the hint to be BINARY with an Enum
    private static final UPayload EMPTY = new UPayload(new byte[0], 0);

    private final byte[] data;

    private final int serializationHint;

    /**
     *
     * @param data
     * @param serializationHint
     */
    public UPayload(byte[] data, int serializationHint) {
        this.data = data;
        this.serializationHint = serializationHint;
    }

    /**
     * The actual serialized or raw data, which can be deserialized or simply used as is using the hint.
     * @return Returns the actual serialized or raw data, which can be deserialized or simply used as is using the hint.
     */
    public byte[] data() {
        return this.data;
    }

    /**
     * A hint to indicate if you can use the data as is in the raw type like a pointer, or the data is serialzied and
     * you might want to deserialize it back into the object form or the Payload.
     * @return
     */
    public int serializationHint() {
        return this.serializationHint;
    }

    public static UPayload empty() {
        return EMPTY;
    }

    public boolean isEmpty() {
        return this.data == null || this.data.length == 0;
    }

    //TODO stick the enum here to make a nice API
}
