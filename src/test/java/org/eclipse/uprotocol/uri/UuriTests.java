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
package org.eclipse.uprotocol.uri;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.uprotocol.uri.serializer.UriSerializer;
import org.eclipse.uprotocol.uri.validator.UriValidator;
import org.eclipse.uprotocol.v1.UUri;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
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
// [utest->dsn~uri-pattern-matching~2]
// TODO: replace with feature from up-spec once the referenced version contains fixes
// see https://github.com/eclipse-uprotocol/up-spec/issues/302
// @SelectFile("up-spec/basics/uuri_pattern_matching.feature")
@SelectClasspathResource("features/uuri_pattern_matching.feature")
// [utest->req~uri-data-model-proto~1]
@SelectFile("up-spec/basics/uuri_protobuf_serialization.feature")
// [utest->req~uri-serialization~1]
// TODO: replace with feature from up-spec once the referenced version contains missing examples
// see https://github.com/eclipse-uprotocol/up-spec/issues/300
// @SelectFile("up-spec/basics/uuri_uri_serialization.feature")
@SelectClasspathResource("features/uuri_uri_serialization.feature")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "org.eclipse.uprotocol.uri")
public class UuriTests {

    private UUri.Builder builder = UUri.newBuilder();
    private UUri uuri;
    private String uri;
    private ByteString protobuf;
    private Exception error;

    private static int getUnsignedInt(String s) {
        if (s.startsWith("0x")) {
            return Integer.parseUnsignedInt(s.substring(2), 16);
        } else {
            return Integer.parseUnsignedInt(s);
        }
    }

    @Given("a URI string {string}")
    public void withUriString(String uriString) {
        this.uri = uriString;
    }

    @Given("a UUri having authority {string}")
    public void withAuthority(String authorityName) {
        builder.setAuthorityName(authorityName);
    }

    @Given("having entity identifier {word}")
    public void withEntityId(String entityId) {
        builder.setUeId(getUnsignedInt(entityId));
    }

    @Given("having major version {word}")
    public void withMajorVersion(String majorVersion) {
        builder.setUeVersionMajor(getUnsignedInt(majorVersion));
    }

    @Given("having resource identifier {word}")
    public void withResourceId(String resourceId) {
        builder.setResourceId(getUnsignedInt(resourceId));
    }

    @When("serializing the UUri to its protobuf wire format")
    public void serializeToProtobuf() {
        uuri = builder.build();
        protobuf = uuri.toByteString();
    }

    @When("serializing the UUri to a URI")
    public void serializeToUri() {
        uuri = builder.build();
        uri = UriSerializer.serialize(uuri, true);
    }

    @When("deserializing the URI to a UUri")
    public void deserializeFromUri() {
        try {
            uuri = UriSerializer.deserialize(uri);
        } catch (Exception e) {
            error = e;
        }
    }

    @Then("the resulting URI string is {word}")
    public void assertUriString(String expectedUri) {
        assertEquals(expectedUri, uri);
    }

    @Then("the original UUri can be recreated from the protobuf wire format")
    public void assertOriginalUuriCanBeRecreatedFromProtobuf() throws InvalidProtocolBufferException {
        var deserializedUuri = UUri.parseFrom(protobuf);
        assertEquals(uuri, deserializedUuri);
    }

    @Then("the same UUri can be deserialized from {word}")
    public void assertUuriCanBeDeserializedFromBytes(String hexString) throws InvalidProtocolBufferException {
        var proto = ByteString.fromHex(hexString);
        var deserializedUuri = UUri.parseFrom(proto);
        assertEquals(uuri, deserializedUuri);
    }

    @Then("the original UUri can be recreated from the URI string")
    public void assertOriginalUuriCanBeRecreatedFromUriString() {
        var deserializedUuri = UriSerializer.deserialize(uri);
        assertEquals(uuri, deserializedUuri);
    }

    @Then("the UUri matches pattern {word}")
    public void assertUuriMatchesPattern(String pattern) {
        assertNotNull(uuri);
        var patternUri = UriSerializer.deserialize(pattern);
        assertTrue(UriValidator.matches(patternUri, uuri));
    }

    @Then("the UUri does not match pattern {word}")
    public void assertUuriDoesNotMatchPattern(String pattern) {
        assertNotNull(uuri);
        var patternUri = UriSerializer.deserialize(pattern);
        assertFalse(UriValidator.matches(patternUri, uuri));
    }

    @Then("the attempt fails")
    public void assertFailure() {
        assertNotNull(error);
    }
}
