/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.eclipse.uprotocol.ubus.mock;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.eclipse.uprotocol.status.datamodel.UStatus;
import org.eclipse.uprotocol.status.factory.UStatusFactory;
import org.eclipse.uprotocol.transport.UMessageReceiver;
import org.eclipse.uprotocol.transport.UTransport;
import org.eclipse.uprotocol.transport.echo.EchoTransport;
import org.eclipse.uprotocol.ubus.UBus;
import org.eclipse.uprotocol.ubus.EventListener;
import org.eclipse.uprotocol.uri.datamodel.UEntity;
import org.eclipse.uprotocol.uri.datamodel.UUri;
import static org.eclipse.uprotocol.cloudevent.factory.UCloudEvent.getSink;
import static org.eclipse.uprotocol.cloudevent.factory.UCloudEvent.getSource;
import static org.eclipse.uprotocol.cloudevent.factory.UCloudEvent.isExpired;
import static org.eclipse.uprotocol.uri.factory.UriFactory.parseFromUri;

import com.google.protobuf.Any;
import com.google.rpc.Code;

import io.cloudevents.CloudEvent;

public class MockUBus implements UBus, UMessageReceiver {

    private final UTransport transport = new EchoTransport(this);
    private final Map<UUri, Set<EventListener>> listenerMap = new HashMap<>();
    private final UEntity name;

    public MockUBus(UEntity name) {
        this.name = name;
    }

    @Override
    public UStatus publish(CloudEvent ce) {
        return Objects.requireNonNullElse(
            transport.send(ce), UStatusFactory.buildUStatus(Code.INVALID_ARGUMENT));
    }

    @Override
    public UStatus notify(CloudEvent ce) {
        return Objects.requireNonNullElse(
            transport.send(ce), UStatusFactory.buildUStatus(Code.INVALID_ARGUMENT));
    }
    
    @Override
    public CompletableFuture<Any> request(CloudEvent ce) {
        transport.send(ce);
        return new CompletableFuture<Any> (); 
    }
    
    @Override
    public UStatus registerEventListener(UUri uri, EventListener listener) {
        try {
            Set<EventListener> listeners = listenerMap.get(uri);
            if (listeners == null) {
                listeners = new HashSet<>();
            }
            if (listeners.isEmpty()) {
                listenerMap.put(uri, listeners);
            }
            listeners.add(listener);
            return UStatusFactory.buildOkUStatus();
        } catch (Exception e) {
            return UStatusFactory.buildUStatus(Code.INVALID_ARGUMENT);
        }
    }


    @Override
    public UStatus unregisterEventListener(UUri uri, EventListener listener) {
        try {
            Set<EventListener> listeners = listenerMap.get(uri);
            if ( (listeners != null) && listeners.contains(listener) ) {
                listeners.remove(listener);
                if (listeners.isEmpty()) {
                    listenerMap.remove(uri);
                }
            }

            return UStatusFactory.buildOkUStatus();
        } catch (Exception e) {
            return UStatusFactory.buildUStatus(Code.INVALID_ARGUMENT);
        }  
    }


    @Override
    public UStatus response(CloudEvent ce) {
        return Objects.requireNonNullElse(
            transport.send(ce),
            UStatusFactory.buildUStatus(Code.INVALID_ARGUMENT));
    }


    @Override
    public UStatus onReceive(CloudEvent ce) {
        final UUri sink = parseFromUri(getSink(ce).orElse(""));
        if (isExpired(ce)) {
            System.out.println(" skipped Expired:" + ce.toString());
            return UStatusFactory.buildUStatus(Code.INVALID_ARGUMENT);
        }

        if ((!sink.isEmpty() && name.name().contains(sink.uEntity().name()))) {                        
            final UUri topic = parseFromUri(getSource(ce));
            final Set<EventListener> listeners = listenerMap.get(topic);
            if ((listeners != null) && !listeners.isEmpty()) {
                listeners.forEach(listener -> listener.onEvent(ce));
            } else {
                System.out.println(" no listener for topic: " + ce.getSource());
            }
        }
        else {
            System.out.println(" skipped: wrong sink: " + ce.toString());
        }
        return UStatusFactory.buildOkUStatus();
    }
}
