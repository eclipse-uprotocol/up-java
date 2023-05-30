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

package org.eclipse.uprotocol.ubus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;

import static org.eclipse.uprotocol.cloudevent.factory.UCloudEvent.getSink;
import static org.eclipse.uprotocol.cloudevent.factory.UCloudEvent.getSource;
import static org.eclipse.uprotocol.cloudevent.factory.UCloudEvent.isExpired;
import static org.eclipse.uprotocol.uri.factory.UriFactory.parseFromUri;

import org.eclipse.uprotocol.transport.Transport;
import org.eclipse.uprotocol.uri.datamodel.UEntity;
import org.eclipse.uprotocol.uri.datamodel.UUri;
import org.eclipse.uprotocol.utils.Receiver;

import static org.eclipse.uprotocol.uri.datamodel.UAuthority.local;
import static org.eclipse.uprotocol.uri.datamodel.UResource.empty;
import static org.eclipse.uprotocol.uri.datamodel.UResource.response;
import static org.eclipse.uprotocol.utils.StatusUtils.STATUS_OK;
import static org.eclipse.uprotocol.utils.StatusUtils.checkArgument;
import static org.eclipse.uprotocol.utils.StatusUtils.checkNotNull;
import static org.eclipse.uprotocol.utils.StatusUtils.throwableToStatus;
import com.google.protobuf.Any;
import com.google.rpc.Status;

import io.cloudevents.CloudEvent;


/* 
 * Abstract bass class forall uBus implmentations. 
 * Specific deployments (i.e. Android, Linux, Cloud, etc..) will inherit from this class and
 * Helper Base class for all uBus implementations. It is expected that you class that is used by uApps and uServices to interface with uBus.
 * uBus. 
 */
public abstract class UBus implements Receiver {

    private Transport mTransport;
    private final UUri mClientUri;
    private final UUri mResponseUri;
    private final Executor mCallbackExecutor;

    private final Object mRegistrationLock = new Object();
    @GuardedBy("mRegistrationLock")
    private final Map<UUri, Set<Receiver>> mReceiverMap = new HashMap<>();


    public UBus(Transport t, UEntity entity, @Nullable Executor executor) {
        validateUEntity();
        mTransport = t;
        mCallbackExecutor = (executor != null) ? executor : Executors.newSingleThreadExecutor();
        mClientUri = new UUri(local(), entity, empty());
        mResponseUri = new UUri(local(), entity, response());
    }

    /**
     * Invoke (call) and RPC
     * @param requestEvent req.v1 CloudEvent.
     * @return Returns the CompletableFuture with the result or exception. Tamara would rather have a CompletionStage since she likes it better
     *  when you program to an interface and not an implementation.
     */
    CompletableFuture<Any> invokeRPC(CloudEvent requestEvent) {
        /* TODO to complete for reference impl */
        return CompletableFuture.failedFuture(new Throwable("TODO"));
    }


    /*
     * Send CloudEvents
     * The cloudevent can be any type (publish, notification, request, etc...)
     */
    Status send(CloudEvent ce) {
        return mTransport.send(ce);
    }


    /*
     * Register Receiver per topic
     * 
     * API shall provide means for uEs to register a Receiver per topic
     * 
     */
    Status registerReceiver(UUri topic, Receiver receiver) {
        try {
            checkArgument(!topic.isEmpty(), "Topic is empty");
            
            synchronized (mRegistrationLock) {
                Set<Receiver> receivers = mReceiverMap.get(topic);
                if (receivers == null) {
                    receivers = new HashSet<>();
                }
                if (receivers.isEmpty()) {
                    mReceiverMap.put(topic, receivers);
                }
                receivers.add(receiver);
                return STATUS_OK;
            }
        } catch (Exception e) {
            return throwableToStatus(e);
        }
    }


    /*
     * Unregister Receiver per topic
     * 
     * API shall provide means for uEs to register a Receiver per topic
     * 
     */
    Status unregisterReceiver(UUri topic, Receiver receiver) {
        try {
            checkArgument(!topic.isEmpty(), "Topic is empty");
            checkNotNull(receiver, "Listener is null");
            synchronized (mRegistrationLock) {
                final Set<Receiver> receivers = mReceiverMap.get(topic);
                if (receivers != null && receivers.contains(receiver)) {
                    receivers.remove(receiver);
                    if (receivers.isEmpty()) {
                        mReceiverMap.remove(topic);
                    }
                }
            }
            return STATUS_OK;
        } catch (Exception e) {
            return throwableToStatus(e);
        }
    }
        

    public void onReceive(CloudEvent ce) {
        final UUri sink = parseFromUri(getSink(ce).orElse(""));       
        if (!isExpired(ce) && (!sink.isEmpty() && !mClientUri.equals(sink))) {
        
            final UUri topic = parseFromUri(getSource(ce));
            mCallbackExecutor.execute(() -> {
                final Set<Receiver> receivers;
                synchronized (mRegistrationLock) {
                    receivers = mReceiverMap.get(topic);
                    if (receivers.isEmpty()) {
                        //Log.w(TAG, UCloudEvent.toString(event) + " skipped: No registered listener");
                    }
                }
                receivers.forEach(receiver -> receiver.onReceive(ce));
            });
        }
        else {
            //Log.w(TAG, UCloudEvent.toString(event) + " skipped: Expired or wrong sink");
        }
    }


    /*
     * Validate the passed entity name matches the callers context.
     * If UEntity name does not match the callers context, the implementation
     * MUST throw java.lang.IllegalAccessException if name and context does not match
     */
    protected abstract void validateUEntity();

}

