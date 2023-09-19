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

/**
 * UUri serializer that will serialize to either String or byte[] the UUri object.
 * 
 * For more information, please refer to https://github.com/eclipse-uprotocol/uprotocol-spec/blob/main/basics/uri.adoc
 * 
 * @param T The serialization formation
 */
public interface UriSerializer<T> {

    /**
     * Deserialize from the format to a UUri
     * @param uri serialized UUri
     * @return deserialized UUri object
     */
    UUri deserialize(T uri);

    /**
     * Serialize from a UUri to the format
     * @param uri UUri object to be serialized to the format T
     * @return serialized UUri
     */
    T serialize(UUri uri);

    /**
     * Long form serializer
     */
    static LongUriSerializer LONG = new LongUriSerializer();
    
    /**
     * Micro form serializer
     */
    static MicroUriSerializer MICRO = new MicroUriSerializer();

    
    /** 
     * Deserialize from a both a long and micro into a resolved UUri
     */
    default UUri deserialize(String longUri, byte[] microUri) {
        
        if (longUri == null || longUri.isEmpty() || microUri == null || microUri.length == 0) {
            return UUri.empty();
        }

        UUri longUUri = LONG.deserialize(longUri);
        UUri microUUri = MICRO.deserialize(microUri);

       
        // Check if the UUris built are valid
        if (!longUUri.isLongForm() || !microUUri.isMicroForm()) {
            return UUri.empty();
        }

        // Both authority types should match (both local or both remote)
        if (longUUri.uAuthority().isLocal() != microUUri.uAuthority().isLocal()) {
            return UUri.empty();
        }


        UAuthority uAuthority = longUUri.uAuthority().isLocal() ? UAuthority.local() :
            UAuthority.resolvedRemote(
                longUUri.uAuthority().device().get(), 
                longUUri.uAuthority().domain().orElse(null),
                microUUri.uAuthority().address().get());
        
        UEntity uEntity = UEntity.resolvedFormat(
            longUUri.uEntity().name(), longUUri.uEntity().version().orElse(null), 
            microUUri.uEntity().id().get());

        UResource uResource = UResource.resolvedFormat(
            longUUri.uResource().name(), 
            longUUri.uResource().instance().orElse(null), 
            longUUri.uResource().message().orElse(null), 
            microUUri.uResource().id().get());
            
        return new UUri(uAuthority, uEntity, uResource);
    }

}
