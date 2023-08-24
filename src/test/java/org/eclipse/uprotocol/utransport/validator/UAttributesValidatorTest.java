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

package org.eclipse.uprotocol.utransport.validator;


import static org.junit.Assert.assertNull;

import org.eclipse.uprotocol.uri.factory.UriFactory;
import org.eclipse.uprotocol.utransport.datamodel.UAttributes;
import org.eclipse.uprotocol.utransport.datamodel.UMessageType;
import org.eclipse.uprotocol.utransport.datamodel.UPriority;
import org.eclipse.uprotocol.utransport.datamodel.USerializationHint;
import org.eclipse.uprotocol.utransport.datamodel.UAttributes.UAttributesBuilder;
import org.eclipse.uprotocol.utransport.validate.UAttributesValidator;
import org.eclipse.uprotocol.uuid.factory.UUIDFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


class UAttributesValidatorTest {

    @Test
    @DisplayName("test fetching validator for valid types")
    public void test_fetching_validator_for_valid_types() {

        UAttributesValidator publish = UAttributesValidator.getValidator(new UAttributesBuilder()
            .withId(UUIDFactory.Factories.UPROTOCOL.factory().create())
            .withPriority(UPriority.LOW)
            .withType(UMessageType.PUBLISH)
            .build());
        assert(publish instanceof UAttributesValidator);

        UAttributesValidator request = UAttributesValidator.getValidator(new UAttributesBuilder()
            .withId(UUIDFactory.Factories.UPROTOCOL.factory().create())
            .withPriority(UPriority.LOW)
            .withType(UMessageType.REQUEST)
            .build());
        assert(request instanceof UAttributesValidator);

        UAttributesValidator response = UAttributesValidator.getValidator(new UAttributesBuilder()
            .withId(UUIDFactory.Factories.UPROTOCOL.factory().create())
            .withPriority(UPriority.LOW)
            .withType(UMessageType.PUBLISH)
            .build());
        assert(response instanceof UAttributesValidator);

        UAttributesValidator invalid = UAttributesValidator.getValidator(new UAttributesBuilder()
            .withId(UUIDFactory.Factories.UPROTOCOL.factory().create())
            .withPriority(UPriority.LOW)
            .build());
        assert(invalid instanceof UAttributesValidator);
    }
    
}
