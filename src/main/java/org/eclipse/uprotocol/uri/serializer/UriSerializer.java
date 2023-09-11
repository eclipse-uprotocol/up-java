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

import org.eclipse.uprotocol.uri.datamodel.UUri;

/**
 * UUri serializer that will serialize to either Long form as a string or short form as a byte[].
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
    public UUri deserialize(T uri);

    /**
     * Serialize from a UUri to the format
     * @param uri UUri object to be serialized to the format T
     * @return serialized UUri
     */
    public T serialize(UUri uri);

    /**
     * Long form serializer
     */
    public static LongUriSerializer LONG = new LongUriSerializer();
    
    /**
     * Micro form serializer
     */
    public static MicroUriSerializer MICRO = new MicroUriSerializer();

}
