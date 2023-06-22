package org.eclipse.uprotocol.topic;

public class LongUriUTopic implements UTopic<String, String> {

    private final UAuthority<String> uAuthority;
    private final UEntity<String> uEntity;
    private final UResource<String> uResource;

    public LongUriUTopic(UAuthority<String> uAuthority, UEntity<String> uEntity, UResource<String> uResource) {
        this.uAuthority = uAuthority;
        this.uEntity = uEntity;
        this.uResource = uResource;
    }

    @Override
    public UAuthority<String> uAuthority() {
        return this.uAuthority;
    }

    @Override
    public UEntity<String> uEntity() {
        return this.uEntity;
    }

    @Override
    public UResource<String> uResource() {
        return this.uResource;
    }
}
