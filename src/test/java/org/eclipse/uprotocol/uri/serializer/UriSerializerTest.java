/*
 * Copyright (c) 2023 General Motors GTO LLC
 *
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
package org.eclipse.uprotocol.uri.serializer;

import com.google.protobuf.ByteString;
import org.eclipse.uprotocol.uri.builder.UResourceBuilder;
import org.eclipse.uprotocol.uri.validator.UriValidator;
import org.eclipse.uprotocol.v1.UAuthority;
import org.eclipse.uprotocol.v1.UEntity;
import org.eclipse.uprotocol.v1.UResource;
import org.eclipse.uprotocol.v1.UUri;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class UriSerializerTest
 {

    @Test
    @DisplayName("Test serialize Uri missing resource")
    public void test_serialize_uri_missing_resource_id() {
        UUri longUUri = UUri.newBuilder()
                .setAuthority(UAuthority.newBuilder().setName("testauth").build())
            .setEntity(UEntity.newBuilder().setName("neelam"))
                .setResource(UResource.newBuilder().setName("rpc").setInstance("response").build())
            .build();
        UUri microUUri = UUri.newBuilder()
                .setEntity(UEntity.newBuilder().setId(29999).setVersionMajor(254))
                .setResource(UResource.newBuilder().setId(39999))
                .build();

        byte[] microuri = MicroUriSerializer.instance().serialize(microUUri);
        String longuri =LongUriSerializer.instance().serialize(longUUri);
        Optional<UUri> resolvedUUri= LongUriSerializer.instance().buildResolved(longuri,microuri);
        assertTrue(resolvedUUri.isPresent());
        assertFalse(UriValidator.isEmpty(resolvedUUri.get()));
        assertEquals("testauth", resolvedUUri.get().getAuthority().getName());
        assertEquals("neelam", resolvedUUri.get().getEntity().getName());
        assertEquals(29999, resolvedUUri.get().getEntity().getId());
        assertEquals(254, resolvedUUri.get().getEntity().getVersionMajor());
        assertEquals("rpc", resolvedUUri.get().getResource().getName());
        assertEquals("response", resolvedUUri.get().getResource().getInstance());
        assertEquals(39999, resolvedUUri.get().getResource().getId());


    }
     @Test
     public void testBuildResolvedWithInvalidInput() {

         // Test the buildResolved method with invalid input
         Optional<UUri> result = MicroUriSerializer.instance().buildResolved(null, null);

         // Assert that the result is empty
         assertTrue(result.isPresent());
         assertTrue(UriValidator.isEmpty(result.get()));

     }
    
}
