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
package org.eclipse.uprotocol.ubus.socket;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;

import javax.sound.midi.Receiver;

import org.eclipse.uprotocol.cloudevent.serialize.CloudEventSerializer;
import org.eclipse.uprotocol.cloudevent.serialize.CloudEventSerializers;
import org.eclipse.uprotocol.transport.MessageReceiver;
import org.eclipse.uprotocol.transport.socket.SocketClient;
import org.eclipse.uprotocol.transport.socket.SocketServer;
import org.eclipse.uprotocol.ubus.EventListener;
import org.eclipse.uprotocol.ubus.Subscriber;
import org.eclipse.uprotocol.ubus.SubscriptionAttributes;
import org.eclipse.uprotocol.uri.datamodel.UEntity;
import org.eclipse.uprotocol.uri.datamodel.UUri;
import static org.eclipse.uprotocol.cloudevent.factory.UCloudEvent.getSink;
import static org.eclipse.uprotocol.cloudevent.factory.UCloudEvent.getSource;
import static org.eclipse.uprotocol.cloudevent.factory.UCloudEvent.isExpired;
import static org.eclipse.uprotocol.cloudevent.factory.UCloudEvent.getPayload;
import static org.eclipse.uprotocol.uri.factory.UriFactory.parseFromUri;
import static org.eclipse.uprotocol.cloudevent.factory.UCloudEvent.getUCloudEventAttributes;

import com.google.rpc.Code;
import com.google.rpc.Status;

import io.cloudevents.CloudEvent;

public class SocketSubscriber implements Subscriber, MessageReceiver {

    private SocketClient client = null;
    private final UEntity name;
    
    private final Map<UUri, Set<EventListener>> listenerMap = new HashMap<>();
    private final CloudEventSerializer serializer = CloudEventSerializers.PROTOBUF.serializer();

    public SocketSubscriber(UEntity _name) {
        name = _name;
        try {
            client = new SocketClient(InetAddress.getLocalHost().getHostName(), this);
        } catch (IOException e) {
            System.out.println("IO Exception occured");
        }
    }

    @Override
    public Status registerEventListener(UUri topic, EventListener listener) {
        try {
            Set<EventListener> listeners = listenerMap.get(topic);
            if (listeners == null) {
                listeners = new HashSet<>();
            }
            if (listeners.isEmpty()) {
                listenerMap.put(topic, listeners);
            }
            listeners.add(listener);
            return Status.newBuilder().setCode(Code.OK_VALUE).build();
        } catch (Exception e) {
            return Status.newBuilder().setCode(Code.INVALID_ARGUMENT_VALUE).build();
        }
    }

    @Override
    public Status unregisterEventListener(UUri topic, EventListener listener) {
        try {
            Set<EventListener> listeners = listenerMap.get(topic);
            if ( (listeners != null) && listeners.contains(listener) ) {
                listeners.remove(listener);
                if (listeners.isEmpty()) {
                    listenerMap.remove(topic);
                }
            }

            return Status.newBuilder().setCode(Code.OK_VALUE).build();
        } catch (Exception e) {
            return Status.newBuilder().setCode(Code.INVALID_ARGUMENT_VALUE).build();
        }
    }

    @Override
    public void onReceive(byte[] message) {
        final CloudEvent ce = serializer.deserialize(message);
        final UUri sink = parseFromUri(getSink(ce).orElse(""));       
        if (!isExpired(ce) && (!sink.isEmpty() && !name.equals(sink))) {                        
            final UUri topic = parseFromUri(getSource(ce));
            final Set<EventListener> listeners = listenerMap.get(topic);
            if (!listeners.isEmpty()) {
                listeners.forEach(listener -> listener.onEvent(
                    parseFromUri(ce.getSource().toString()), getPayload(ce).toByteArray(), getUCloudEventAttributes(ce).get()));
            }
        }
        else {
            System.out.println(" skipped: Expired or wrong sink");
        }
    }
}
