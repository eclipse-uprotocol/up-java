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

public enum MessageType {
    PUBLISH(0, "pub.v1"),   // Publish or notification event
    REQUEST(1, "req.v1"),   // Request
    RESPONSE(2, "res.v1");  // Response

    private final int value;
    private final String name;

    public int typeValue() {
        return value;
    }

    public String typeName() {
        return name;
    }

    MessageType(int value, String name) {
        this.value = value;
        this.name = name;
    }
}
