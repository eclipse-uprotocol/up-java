/**
 * SPDX-FileCopyrightText: 2025 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.eclipse.uprotocol.uuid;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.eclipse.uprotocol.uuid.serializer.UuidSerializer;
import org.eclipse.uprotocol.v1.UUID;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectFile;
import org.junit.platform.suite.api.Suite;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;

@Suite
@IncludeEngines("cucumber")
// [utest->req~uuid-proto~1]
@SelectFile("up-spec/basics/uuid_protobuf_serialization.feature")
// [utest->req~uuid-hex-and-dash~1]
@SelectFile("up-spec/basics/uuid_string_serialization.feature")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "org.eclipse.uprotocol.uuid")
public class UuidTests {
    private UUID uuid;
    private String hyphenatedString;
    private ByteString protobuf;
    private Exception error;

    private static long getUnsignedLong(String s) {
        if (s.startsWith("0x")) {
            return Long.parseUnsignedLong(s.substring(2), 16);
        } else {
            return Long.parseUnsignedLong(s);
        }
    }

    @Given("a UUID having MSB {word} and LSB {word}")
    public void withMsbLsb(String msbHexString, String lsbHexString) {
        this.uuid = UUID.newBuilder()
            .setMsb(getUnsignedLong(msbHexString))
            .setLsb(getUnsignedLong(lsbHexString))
            .build();
    }

    @Given("a UUID string representation {word}")
    public void withHyphenatedString(String hyphenatedString) {
        this.hyphenatedString = hyphenatedString;
    }

    @When("serializing the UUID to its protobuf wire format")
    public void serializeToProtobuf() {
        this.protobuf = uuid.toByteString();
    }

    @When("serializing the UUID to a hyphenated string")
    public void serializeToHyphenatedString() {
        this.hyphenatedString = UuidSerializer.serialize(uuid);
    }

    @When("deserializing the hyphenated string to a UUID")
    public void deserializeFromHyphenatedString() {
        try {
            this.uuid = UuidSerializer.deserialize(hyphenatedString);
        } catch (Exception e) {
            error = e;
        }
    }

    @Then("the resulting hyphenated string is {word}")
    public void assertHyphenatedString(String expectedString) {
        assertEquals(this.hyphenatedString, expectedString);
    }

    @Then("the original UUID can be recreated from the protobuf wire format")
    public void assertOriginalUuidCanBeRecreatedFromProtobuf() throws InvalidProtocolBufferException {
        UUID recreatedUuid = UUID.parseFrom(protobuf);
        assertEquals(this.uuid, recreatedUuid);
    }

    @Then("the same UUID can be deserialized from {word}")
    public void assertDeserializeUuidFromProtobuf(String hexString) throws InvalidProtocolBufferException {
        var deserializedUuid = UUID.parseFrom(ByteString.fromHex(hexString));
        assertEquals(this.uuid, deserializedUuid);
    }

    @Then("the original UUID can be recreated from the hyphenated string")
    public void assertOriginalUuidCanBeRecreatedFromHyphenatedString() {
        assertEquals(this.uuid, UuidSerializer.deserialize(hyphenatedString));
    }

    @Then("the attempt fails")
    public void assertFailure() {
        assertNotNull(this.error);
    }
}
