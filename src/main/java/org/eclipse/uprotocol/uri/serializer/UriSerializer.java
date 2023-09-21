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

import org.eclipse.uprotocol.uri.datamodel.UAuthority;
import org.eclipse.uprotocol.uri.datamodel.UEntity;
import org.eclipse.uprotocol.uri.datamodel.UResource;
import org.eclipse.uprotocol.uri.datamodel.UUri;

import java.util.Optional;

/**
 * UUris are used in transport layers and hence need to be serialised.
 * Each transport supports different serialisation formats.
 * For more information, please refer to <a href="https://github.com/eclipse-uprotocol/uprotocol-spec/blob/main/basics/uri.adoc">...</a>
 * @param <T> The data structure that the UUri will be serialised into. For example String or byte[].
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
            return Optional.of(UUri.empty());
        }

        UUri longUUri = LongUriSerializer.instance().deserialize(longUri);
        UUri microUUri = MicroUriSerializer.instance().deserialize(microUri);

        UAuthority uAuthority = longUUri.uAuthority().isLocal() ? UAuthority.local() :
            UAuthority.resolvedRemote(
                longUUri.uAuthority().device().orElse(null),
                longUUri.uAuthority().domain().orElse(null),
                microUUri.uAuthority().address().orElse(null));
        
        UEntity uEntity = UEntity.resolvedFormat(
            longUUri.uEntity().name(), longUUri.uEntity().version().orElse(null), 
            microUUri.uEntity().id().orElse(null));

        UResource uResource = UResource.resolvedFormat(
            longUUri.uResource().name(), 
            longUUri.uResource().instance().orElse(null), 
            longUUri.uResource().message().orElse(null), 
            microUUri.uResource().id().orElse(null));
            
        UUri uUri = new UUri(uAuthority, uEntity, uResource);
        return uUri.isResolved() ? Optional.of(uUri) : Optional.empty();
    }

}
