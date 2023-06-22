package org.eclipse.uprotocol.topic;

public interface UTopic<T, U> {

    UAuthority<T> uAuthority();
    UEntity<U> uEntity();
    UResource<U> uResource();
}
