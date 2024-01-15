/*
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
* SPDX-FileType: SOURCE
* SPDX-FileCopyrightText: 2023 General Motors GTO LLC
* SPDX-License-Identifier: Apache-2.0
*/

package org.eclipse.uprotocol.uri.builder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.uprotocol.v1.*;
import org.eclipse.uprotocol.example.hello_world.v1.HelloWorldProto;
import org.eclipse.uprotocol.example.hello_world.v1.Timer;

import org.eclipse.uprotocol.uri.serializer.LongUriSerializer;
import org.eclipse.uprotocol.uri.serializer.MicroUriSerializer;
import org.eclipse.uprotocol.uri.validator.UriValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class UUriBuilderTest {
    @Test
    @DisplayName("Test building the Helloworld timer topic so we can subscribe to said topic")
    public void test_build_uresource_from_protobuf_message_enum() {
        // Build UEntity from Proto generated code
        UEntity entity = UEntityBuilder.fromProto(HelloWorldProto.getDescriptor().getServices().get(0));

        // Build UResource from Proto generated code
        UResource resource = UResourceBuilder.fromProto(Timer.Resources.one_second);
        
        // Build a fully resolved (local) UUri        
        UUri topic = UUri.newBuilder()
            .setEntity(entity)
            .setResource(resource)
            .build();

        assertTrue(UriValidator.isMicroForm(topic));
        assertTrue(UriValidator.isLongForm(topic));
        assertTrue(UriValidator.isResolved(topic));
        
        String longTopic = LongUriSerializer.instance().serialize(topic);
        byte[] microTopic = MicroUriSerializer.instance().serialize(topic);
        assertEquals(longTopic, "/example.hello_world/1/one_second#Timer");
    }
}
