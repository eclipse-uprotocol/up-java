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
package org.eclipse.uprotocol.testues.mock;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.concurrent.CompletableFuture;

import org.eclipse.uprotocol.cloudevent.datamodel.UCloudEventAttributes;
import org.eclipse.uprotocol.cloudevent.factory.CloudEventFactory;
import org.eclipse.uprotocol.status.datamodel.UStatus;
import org.eclipse.uprotocol.status.factory.UStatusFactory;
import org.eclipse.uprotocol.ubus.EventListener;
import org.eclipse.uprotocol.ubus.mock.MockUBus;
import org.eclipse.uprotocol.uri.datamodel.UAuthority;
import org.eclipse.uprotocol.uri.datamodel.UEntity;
import org.eclipse.uprotocol.uri.datamodel.UResource;
import org.eclipse.uprotocol.uri.datamodel.UUri;
import org.eclipse.uprotocol.uri.factory.UriFactory;

import com.google.protobuf.Any;

import io.cloudevents.CloudEvent;

public class MockUApp implements Runnable, EventListener {

    private MockUBus ubus;
    private final UEntity use = new UEntity("HartleyApp", "1.0");
    
    @Override
    public UStatus onEvent(CloudEvent ce) {
        System.out.println("uApp Received Event " + ce.toString());
        return UStatusFactory.buildOkUStatus();
    }

    @Override
    public void run() {

        System.out.println("Starting Mock uApp");
                
        // service Method Uri
        UEntity methodSoftwareEntityService = new UEntity("body.access", "1");
        UUri methodUri = new UUri(UAuthority.local(), methodSoftwareEntityService,
                UResource.forRpc("UpdateDoor"));
        
        String responseUri = UriFactory.buildUriForRpc(UAuthority.local(), use);
        
        ubus = new MockUBus(use);

        ubus.registerEventListener(UriFactory.parseFromUri(responseUri), this);
        
        CompletableFuture<Any> response = ubus.request(
            CloudEventFactory.request(
                UriFactory.buildUriForRpc(UAuthority.local(), use),
                    methodUri.uProtocolUri(), 
                Any.newBuilder().build(),
                new UCloudEventAttributes.UCloudEventAttributesBuilder()
                    .withHash("somehash")
                    .withPriority(UCloudEventAttributes.Priority.OPERATIONS)
                    .withTtl(3)
                    .withToken("someOAuthToken")
                    .build()));
        
        response.join();
    }


    public static void main(String[] args) throws UnknownHostException, IOException, ClassNotFoundException, InterruptedException {
        final MockUApp app = new MockUApp();

        app.run();
        app.wait();
    }

}
