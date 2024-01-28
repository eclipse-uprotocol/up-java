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
 * SPDX-FileType: SOURCE
 * SPDX-FileCopyrightText: 2023 General Motors GTO LLC
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.uprotocol.uri.serializer;


import java.util.Optional;

import org.eclipse.uprotocol.uri.validator.UriValidator;
import org.eclipse.uprotocol.v1.UAuthority;
import org.eclipse.uprotocol.v1.UEntity;
import org.eclipse.uprotocol.v1.UResource;
import org.eclipse.uprotocol.v1.UUri;

/**
 * UUris are used in transport layers and hence need to be serialized.
 * Each transport supports different serialization formats.
 * For more information, please refer to <a href="https://github.com/eclipse-uprotocol/uprotocol-spec/blob/main/basics/uri.adoc">...</a>
 * @param <T> The data structure that the UUri will be serialized into. For example String or byte[].
 */
public interface UriSerializer<T> {

    /**
     * Deserialize from the format to a {@link UUri}.
     * @param uri serialized UUri.
     * @return Returns a {@link UUri} object from the serialized format from the wire.
     */
    UUri deserialize(T uri);

    /**
     * Serialize from a {@link UUri} to a specific serialization format.
     * @param uri UUri object to be serialized to the format T.
     * @return Returns the {@link UUri} in the transport serialized format.
     */
    T serialize(UUri uri);

    /**
     * Build a fully resolved {@link UUri} from the serialized long format and the serializes micro format.
     * @param longUri {@link UUri} serialized as a Sting.
     * @param microUri {@link UUri} serialized as a byte[].
     * @return Returns a {@link UUri} object serialized from one of the forms.
     */
    default Optional<UUri> buildResolved(String longUri, byte[] microUri) {
        
        if ((longUri == null || longUri.isEmpty()) && (microUri == null || microUri.length == 0)) {
            return Optional.of(UUri.getDefaultInstance());
        }

        UUri longUUri = LongUriSerializer.instance().deserialize(longUri);
        UUri microUUri = MicroUriSerializer.instance().deserialize(microUri);
 
        final UAuthority.Builder uAuthorityBuilder = 
            UAuthority.newBuilder(microUUri.getAuthority())
                .setName(longUUri.getAuthority().getName());
 
        
        final UEntity.Builder UEntityFactory = UEntity.newBuilder(microUUri.getEntity())
            .setName(longUUri.getEntity().getName());
            

        final UResource.Builder uResourceBuilder = UResource.newBuilder(longUUri.getResource())
            .setId(microUUri.getResource().getId());
            
        UUri uUri = UUri.newBuilder()
            .setAuthority(uAuthorityBuilder)
            .setEntity(UEntityFactory)
            .setResource(uResourceBuilder)
            .build();
        return UriValidator.isResolved(uUri) ? Optional.of(uUri) : Optional.empty();
        }

}
