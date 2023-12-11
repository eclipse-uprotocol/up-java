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

package org.eclipse.uprotocol.uri;
import org.eclipse.uprotocol.v1.UAuthority;

import java.util.Optional;

/**
 * Uuri Utils class that provides utility methods for UUri
 */
public interface UuriUtils {

    /**
     * Extracts the VIN from the given UAuthority.
     *
     * @param uAuthority The UAuthority object from which to extract the VIN.
     * @return An Optional containing the extracted VIN if available, or an empty Optional if the
     * UAuthority is null, has no name.
     */
    static Optional<String> extractVinFromUAuthority(UAuthority uAuthority) {
        return Optional.ofNullable(uAuthority).filter(UAuthority::hasName).map(UAuthority::getName)
                .map(domain -> domain.split("[.]", 0)).filter(parts -> parts.length > 0).map(parts -> parts[0]);

    }

}
