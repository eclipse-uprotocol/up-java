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

package org.eclipse.uprotocol.cloudevent.datamodel;

import java.util.Arrays;
import java.util.Optional;

/**
 * Enumeration for the core types of uProtocol CloudEvents.
 */
public enum UCloudEventType {
    PUBLISH ("pub.v1"),
    REQUEST ("req.v1"),
    RESPONSE ("res.v1");

    public String type() {
        return type;
    }

    /**
     * Convert a String type into a maybe UCloudEventType.
     * @param type The String value of the UCloudEventType.
     * @return returns the UCloudEventType associated with the provided String.
     */
    public static Optional<UCloudEventType> valueOfType(String type) {
        return Arrays.stream(UCloudEventType.values())
                .filter(ceType -> ceType.type().equals(type))
                .findAny();
    }

    private final String type;

    UCloudEventType(String type) {
        this.type = type;
    }
}
