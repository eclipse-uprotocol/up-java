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

import org.eclipse.uprotocol.cloudevent.datamodel.UCloudEventAttributes;
import org.eclipse.uprotocol.cloudevent.factory.CloudEventFactory;
import org.eclipse.uprotocol.cloudevent.factory.UCloudEvent;
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

public class MockUService implements EventListener, Runnable {

    private MockUBus uBus;
    
    private final UEntity uEName = new UEntity("HartleyService", "1.0");

    @Override
    public UStatus onEvent(CloudEvent ce) {
        // This is where we implement the request then generate a reply
        System.out.println("Received Request " + ce.toString());

        if ( (uBus != null) && (ce != null) && (!UCloudEvent.isExpired(ce)) &&
             UCloudEvent.getSink(ce).isPresent() && UCloudEvent.getRequestId(ce).isPresent()) {
            uBus.response(
                CloudEventFactory.response(
                    UCloudEvent.getSink(ce).orElse(""),        // Source for the response
                    UCloudEvent.getSource(ce),      // Sink of the Response
                    UCloudEvent.getRequestId(ce).orElse("UUID"), 
                Any.newBuilder().build(), 
                new UCloudEventAttributes.UCloudEventAttributesBuilder()
                    .withHash("somehash")
                    .withPriority(UCloudEventAttributes.Priority.OPERATIONS)
                    .withTtl(3)
                    .withToken("someOAuthToken")
                    .build()));

        }

        return UStatusFactory.buildOkUStatus();

    }

    @Override
    public void run() {
        System.out.println("Starting Mock uService");

        uBus = new MockUBus(uEName);
        
        // service Method Uri
        UUri methodUri = new UUri(UAuthority.local(), uEName,
                UResource.forRpc("EchoHello"));

        uBus.registerEventListener(methodUri, this);
                
        // Test sending a publish Event
        UUri topic = new UUri(UAuthority.local(), uEName,
                new UResource("hello", "world", "Message"));
        
        String consumer = UriFactory.buildUriForRpc(UAuthority.local(), new UEntity("HartleyApp", "1.1"));
        
        while (true) {
            // Test publish event
            uBus.publish(
                CloudEventFactory.publish(
                    UriFactory.buildUProtocolUri(topic), 
                    Any.newBuilder().build(), 
                    new UCloudEventAttributes.UCloudEventAttributesBuilder()
                        .withHash("somehash")
                        .withPriority(UCloudEventAttributes.Priority.OPERATIONS)
                        .withTtl(3)
                        .withToken("someOAuthToken")
                        .build()));

            // Send notification
            uBus.notify(
                CloudEventFactory.notification(
                    UriFactory.buildUProtocolUri(topic), 
                    consumer,
                    Any.newBuilder().build(), 
                    new UCloudEventAttributes.UCloudEventAttributesBuilder()
                        .withHash("somehash")
                        .withPriority(UCloudEventAttributes.Priority.OPERATIONS)
                        .withTtl(3)
                        .withToken("someOAuthToken")
                        .build()));

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }


    public static void main(String[] args) throws UnknownHostException, IOException, ClassNotFoundException, InterruptedException {
        final MockUService service = new MockUService();
        service.run();
        service.wait();
    }
}
