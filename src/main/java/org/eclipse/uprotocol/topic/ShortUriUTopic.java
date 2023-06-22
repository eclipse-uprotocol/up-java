package org.eclipse.uprotocol.topic;

import java.net.InetAddress;

public class ShortUriUTopic implements UTopic<InetAddress,Integer> {

    private final UAuthority<InetAddress> uAuthority;
    private final UEntity<Integer> uEntity;
    private final UResource<Integer> uResource;

    public ShortUriUTopic(UAuthority<InetAddress> uAuthority, UEntity<Integer> uEntity, UResource<Integer> uResource) {
        this.uAuthority = uAuthority;
        this.uEntity = uEntity;
        this.uResource = uResource;
    }

    @Override
    public UAuthority<InetAddress> uAuthority() {
        return this.uAuthority;
    }

    @Override
    public UEntity<Integer> uEntity() {
        return this.uEntity;
    }

    @Override
    public UResource<Integer> uResource() {
        return this.uResource;
    }
}
