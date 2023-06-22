package org.eclipse.uprotocol.topic;

public interface UResource<T> {
    T getResource();

    boolean isRpcMethod();
}
