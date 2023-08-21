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

package org.eclipse.uprotocol.utransport;

import org.eclipse.uprotocol.uri.datamodel.UAuthority;
import org.eclipse.uprotocol.uri.datamodel.UEntity;
import org.eclipse.uprotocol.uri.datamodel.UResource;
import org.eclipse.uprotocol.uri.datamodel.UUri;

/**
 * Instead of changing the library at this moment, I am just creating a wrapper class.
 */
public class UTopic extends UUri {

    private static final UTopic EMPTY = new UTopic(UAuthority.empty(), UEntity.empty(), UResource.empty());

    public UTopic(UAuthority uAuthority, UEntity uEntity, UResource uResource) {
        super(uAuthority, uEntity, uResource);
    }

    public static UTopic empty() {
        return EMPTY;
    }

    @Override
    public String toString() {
        return "UTopic{uAuthority=" + uAuthority() + ", uEntity=" + uEntity() + ", uResource=" + uResource() + "}";
    }

    public String toLog() {
        return String.format("UTopic: %s/%s/%s/%s/%s/%s/%s",
                uAuthority().device().orElse("local"), uAuthority().domain().orElse(""),
                uEntity().name(), uEntity().version().orElse(""),
                uResource().name(), uResource().instance().orElse(""), uResource().message().orElse(""));
    }
}
