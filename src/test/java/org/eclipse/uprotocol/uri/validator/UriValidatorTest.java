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

package org.eclipse.uprotocol.uri.validator;


import org.eclipse.uprotocol.uri.factory.UResourceBuilder;
import org.eclipse.uprotocol.uri.serializer.LongUriSerializer;
import org.eclipse.uprotocol.v1.UAuthority;
import org.eclipse.uprotocol.v1.UEntity;
import org.eclipse.uprotocol.v1.UResource;
import org.eclipse.uprotocol.v1.UUri;
import org.eclipse.uprotocol.validation.ValidationResult;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.protobuf.ByteString;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UriValidatorTest {

    @Test
    @DisplayName("Test validate blank uri")
    public void test_validate_blank_uri() {
        final UUri uri = LongUriSerializer.instance().deserialize(null);
        final ValidationResult status = UriValidator.validate(uri);
        assertTrue(UriValidator.isEmpty(uri));
        assertEquals("Uri is empty.", status.getMessage());
    }

    @Test
    @DisplayName("Test validate uri with no device name")
    public void test_validate_uri_with_no_entity_getName() {
        final UUri uri = LongUriSerializer.instance().deserialize("//");
        final ValidationResult status = UriValidator.validate(uri);
        assertTrue(UriValidator.isEmpty(uri));
        assertEquals("Uri is empty.", status.getMessage());
    }

    @Test
    @DisplayName("Test validate uri with uEntity")
    public void test_validate_uri_with_getEntity() {
        final UUri uri = LongUriSerializer.instance().deserialize("/hartley");
        final ValidationResult status = UriValidator.validate(uri);
        assertEquals(ValidationResult.success(), status);
    }

    @Test
    @DisplayName("Test validate with malformed URI")
    public void test_validate_with_malformed_uri() {
        final UUri uri = LongUriSerializer.instance().deserialize("hartley");
        final ValidationResult status = UriValidator.validate(uri);
        assertTrue(UriValidator.isEmpty(uri));
        assertEquals("Uri is empty.", status.getMessage());
    }


    @Test
    @DisplayName("Test validate with blank UEntity Name")
    public void test_validate_with_blank_uentity_name_uri() {
        final ValidationResult status = UriValidator.validate(UUri.getDefaultInstance());
        assertTrue(status.isFailure());
        assertEquals("Uri is empty.", status.getMessage());
    }

    @Test
    @DisplayName("Test validateRpcMethod with valid URI")
    public void test_validateRpcMethod_with_valid_uri() {
        final UUri uri = LongUriSerializer.instance().deserialize("/hartley//rpc.echo");
        final ValidationResult status = UriValidator.validateRpcMethod(uri);
        assertEquals(ValidationResult.success(), status);
    }

    @Test
    @DisplayName("Test validateRpcMethod with invalid URI")
    public void test_validateRpcMethod_with_invalid_uri() {
        final UUri uri = LongUriSerializer.instance().deserialize("/hartley/echo");
        final ValidationResult status = UriValidator.validateRpcMethod(uri);
        assertTrue(status.isFailure());
        assertEquals("Uri is empty.", status.getMessage());
    }

    @Test
    @DisplayName("Test validateRpcMethod with malformed URI")
    public void test_validateRpcMethod_with_malformed_uri() {
        final UUri uri = LongUriSerializer.instance().deserialize("hartley");
        final ValidationResult status = UriValidator.validateRpcMethod(uri);
        assertTrue(UriValidator.isEmpty(uri));
        assertEquals("Uri is empty.", status.getMessage());
    }

    @Test
    @DisplayName("Test validateRpcResponse with valid URI")
    public void test_validateRpcResponse_with_valid_uri() {
        final UUri uri = LongUriSerializer.instance().deserialize("/hartley//rpc.response");
        final ValidationResult status = UriValidator.validateRpcResponse(uri);
        assertEquals(ValidationResult.success(), status);
    }

    @Test
    @DisplayName("Test validateRpcResponse with malformed URI")
    public void test_validateRpcResponse_with_malformed_uri() {
        final UUri uri = LongUriSerializer.instance().deserialize("hartley");
        final ValidationResult status = UriValidator.validateRpcResponse(uri);
        assertTrue(UriValidator.isEmpty(uri));
        assertEquals("Uri is empty.", status.getMessage());
    }

    @Test
    @DisplayName("Test validateRpcResponse with rpc type")
    public void test_validateRpcResponse_with_rpc_type() {
        final UUri uri = LongUriSerializer.instance().deserialize("/hartley//dummy.wrong");
        final ValidationResult status = UriValidator.validateRpcResponse(uri);
        assertTrue(status.isFailure());
        assertEquals("Invalid RPC response type.", status.getMessage());
    }

    @Test
    @DisplayName("Test validateRpcResponse with invalid rpc response type")
    public void test_validateRpcResponse_with_invalid_rpc_response_type() {
        final UUri uri = LongUriSerializer.instance().deserialize("/hartley//rpc.wrong");
        final ValidationResult status = UriValidator.validateRpcResponse(uri);
        assertTrue(status.isFailure());
        assertEquals("Invalid RPC response type.", status.getMessage());
    }

    @Test
    @DisplayName("Test validate topic uri with version, when it is valid microRemote")
    void test_topic_uri_with_version_when_it_is_valid_remote() {

        final String uri = "//VCU.MY_CAR_VIN/body.access/1/door.front_left#Door";

        final ValidationResult status = UriValidator.validate(LongUriSerializer.instance().deserialize(uri));
        assertTrue(status.isSuccess());
    }

    @Test
    @DisplayName("Test validate topic uri no version, when it is valid microRemote")
    void test_topic_uri_no_version_when_it_is_valid_remote() {

        final String uri = "//VCU.MY_CAR_VIN/body.access//door.front_left#Door";

        final ValidationResult status = UriValidator.validate(LongUriSerializer.instance().deserialize(uri));
        assertTrue(status.isSuccess());
    }

    @Test
    @DisplayName("Test validate topic uri with version, when it is valid local")
    void test_topic_uri_with_version_when_it_is_valid_local() {

        final String uri = "/body.access/1/door.front_left#Door";

        final ValidationResult status = UriValidator.validate(LongUriSerializer.instance().deserialize(uri));
        assertTrue(status.isSuccess());
    }

    @Test
    @DisplayName("Test validate topic uri no version, when it is valid local")
    void test_topic_uri_no_version_when_it_is_valid_local() {

        final String uri = "/body.access//door.front_left#Door";

        final ValidationResult status = UriValidator.validate(LongUriSerializer.instance().deserialize(uri));
        assertTrue(status.isSuccess());
    }

    @Test
    @DisplayName("Test validate topic uri is invalid when uri contains nothing but schema")
    void test_topic_uri_invalid_when_uri_has_schema_only() {

        final String uri = ":";

        final ValidationResult status = UriValidator.validate(LongUriSerializer.instance().deserialize(uri));
        assertTrue(status.isFailure());
    }

    @Test
    @DisplayName("Test validate topic uri is invalid when uri contains empty use name local")
    void test_topic_uri_invalid_when_uri_has_empty_use_name_local() {

        final String uri = "/";

        final ValidationResult status = UriValidator.validate(LongUriSerializer.instance().deserialize(uri));
        assertTrue(status.isFailure());
    }

    @Test
    @DisplayName("Test validate topic uri is invalid when uri is microRemote but missing authority")
    void test_topic_uri_invalid_when_uri_is_remote_no_authority() {

        final String uri = "//";

        final ValidationResult status = UriValidator.validate(LongUriSerializer.instance().deserialize(uri));
        assertTrue(status.isFailure());
    }

    @Test
    @DisplayName("Test validate topic uri is invalid when uri is microRemote with use but missing authority")
    void test_topic_uri_invalid_when_uri_is_remote_no_authority_with_use() {

        final String uri = "///body.access/1/door.front_left#Door";

        final ValidationResult status = UriValidator.validate(LongUriSerializer.instance().deserialize(uri));

        assertTrue(status.isFailure());

    }

    @Test
    @DisplayName("Test validate topic uri is invalid when uri has no use information")
    void test_topic_uri_invalid_when_uri_is_missing_use_remote() {

        final String uri = "//VCU.myvin///door.front_left#Door";

        final ValidationResult status = UriValidator.validate(LongUriSerializer.instance().deserialize(uri));

        assertTrue(status.isFailure());
    }

    @Test
    @DisplayName("Test validate microRemote topic uri is invalid when uri is missing use name")
    void test_topic_uri_invalid_when_uri_is_missing_use_name_remote() {

        final String uri = "/1/door.front_left#Door";

        final ValidationResult status = UriValidator.validate(LongUriSerializer.instance().deserialize(uri));

        assertTrue(status.isFailure());
    }

    @Test
    @DisplayName("Test validate local topic uri is invalid when uri is missing use name")
    void test_topic_uri_invalid_when_uri_is_missing_use_name_local() {

        final String uri = "//VCU.myvin//1";

        final ValidationResult status = UriValidator.validate(LongUriSerializer.instance().deserialize(uri));

        assertTrue(status.isFailure());
    }


    @Test
    @DisplayName("Test validate rpc topic uri with version, when it is valid microRemote")
    void test_rpc_topic_uri_with_version_when_it_is_valid_remote() {

        final String uri = "//bo.cloud/petapp/1/rpc.response";

        final ValidationResult status = UriValidator.validateRpcMethod(LongUriSerializer.instance().deserialize(uri));
        assertTrue(status.isSuccess());
    }

    @Test
    @DisplayName("Test validate rpc topic uri no version, when it is valid microRemote")
    void test_rpc_topic_uri_no_version_when_it_is_valid_remote() {

        final String uri = "//bo.cloud/petapp//rpc.response";

        final ValidationResult status = UriValidator.validateRpcMethod(LongUriSerializer.instance().deserialize(uri));
        assertTrue(status.isSuccess());
    }

    @Test
    @DisplayName("Test validate rpc topic uri with version, when it is valid local")
    void test_rpc_topic_uri_with_version_when_it_is_valid_local() {

        final String uri = "/petapp/1/rpc.response";

        final ValidationResult status = UriValidator.validateRpcMethod(LongUriSerializer.instance().deserialize(uri));
        assertTrue(status.isSuccess());
    }

    @Test
    @DisplayName("Test validate rpc topic uri no version, when it is valid local")
    void test_rpc_topic_uri_no_version_when_it_is_valid_local() {

        final String uri = "/petapp//rpc.response";

        final ValidationResult status = UriValidator.validateRpcMethod(LongUriSerializer.instance().deserialize(uri));
        assertTrue(status.isSuccess());
    }

    @Test
    @DisplayName("Test validate rpc topic uri is invalid when uri contains nothing but schema")
    void test_rpc_topic_uri_invalid_when_uri_has_schema_only() {

        final String uri = ":";

        final ValidationResult status = UriValidator.validateRpcMethod(LongUriSerializer.instance().deserialize(uri));

        assertTrue(status.isFailure());
    }

    @Test
    @DisplayName("Test validate rpc topic uri with version, when it is local but missing rpc.respons")
    void test_rpc_topic_uri_with_version_when_it_is_not_valid_missing_rpc_response_local() {

        final String uri = "/petapp/1/dog";

        final ValidationResult status = UriValidator.validateRpcMethod(LongUriSerializer.instance().deserialize(uri));

        assertTrue(status.isFailure());
    }

    @Test
    @DisplayName("Test validate rpc topic uri with version, when it is microRemote but missing rpc.respons")
    void test_rpc_topic_uri_with_version_when_it_is_not_valid_missing_rpc_response_remote() {

        final String uri = "//petapp/1/dog";

        final ValidationResult status = UriValidator.validateRpcMethod(LongUriSerializer.instance().deserialize(uri));

        assertTrue(status.isFailure());
    }

    @Test
    @DisplayName("Test validate rpc topic uri is invalid when uri is microRemote but missing authority")
    void test_rpc_topic_uri_invalid_when_uri_is_remote_no_authority() {

        final String uri = "//";

        final ValidationResult status = UriValidator.validateRpcMethod(LongUriSerializer.instance().deserialize(uri));

        assertTrue(status.isFailure());
    }

    @Test
    @DisplayName("Test validate rpc topic uri is invalid when uri is microRemote with use but missing authority")
    void test_rpc_topic_uri_invalid_when_uri_is_remote_no_authority_with_use() {
        final String uri = "///body.access/1";
        final ValidationResult status = UriValidator.validateRpcMethod(LongUriSerializer.instance().deserialize(uri));
        assertTrue(status.isFailure());
    }

    @Test
    @DisplayName("Test validate rpc topic uri is invalid when uri has no use information")
    void test_rpc_topic_uri_invalid_when_uri_is_missing_use() {
        final String uri = "//VCU.myvin";
        final ValidationResult status = UriValidator.validateRpcMethod(LongUriSerializer.instance().deserialize(uri));
        assertTrue(status.isFailure());
    }

    @Test
    @DisplayName("Test validate microRemote rpc topic uri is invalid when uri is missing use name")
    void test_rpc_topic_uri_invalid_when_uri_is_missing_use_name_remote() {
        final String uri = "/1";
        final ValidationResult status = UriValidator.validateRpcMethod(LongUriSerializer.instance().deserialize(uri));
        assertTrue(status.isFailure());
    }

    @Test
    @DisplayName("Test validate local rpc topic uri is invalid when uri is missing use name")
    void test_rpc_topic_uri_invalid_when_uri_is_missing_use_name_local() {
        final String uri = "//VCU.myvin//1";
        final ValidationResult status = UriValidator.validateRpcMethod(LongUriSerializer.instance().deserialize(uri));
        assertTrue(status.isFailure());
    }


    @Test
    @DisplayName("Test validate rpc method uri with version, when it is valid microRemote")
    void test_rpc_method_uri_with_version_when_it_is_valid_remote() {
        final String uri = "//VCU.myvin/body.access/1/rpc.UpdateDoor";
        final ValidationResult status = UriValidator.validateRpcMethod(LongUriSerializer.instance().deserialize(uri));
        assertTrue(status.isSuccess());
    }

    @Test
    @DisplayName("Test validate rpc method uri no version, when it is valid microRemote")
    void test_rpc_method_uri_no_version_when_it_is_valid_remote() {
        final String uri = "//VCU.myvin/body.access//rpc.UpdateDoor";
        final ValidationResult status = UriValidator.validateRpcMethod(LongUriSerializer.instance().deserialize(uri));
        assertTrue(status.isSuccess());
    }

    @Test
    @DisplayName("Test validate rpc method uri with version, when it is valid local")
    void test_rpc_method_uri_with_version_when_it_is_valid_local() {
        final String uri = "/body.access/1/rpc.UpdateDoor";
        final ValidationResult status = UriValidator.validateRpcMethod(LongUriSerializer.instance().deserialize(uri));
        assertTrue(status.isSuccess());
    }

    @Test
    @DisplayName("Test validate rpc method uri no version, when it is valid local")
    void test_rpc_method_uri_no_version_when_it_is_valid_local() {
        final String uri = "/body.access//rpc.UpdateDoor";
        final ValidationResult status = UriValidator.validateRpcMethod(LongUriSerializer.instance().deserialize(uri));
        assertTrue(status.isSuccess());
    }

    @Test
    @DisplayName("Test validate rpc method uri is invalid when uri contains nothing but schema")
    void test_rpc_method_uri_invalid_when_uri_has_schema_only() {
        final String uri = ":";
        final ValidationResult status = UriValidator.validateRpcMethod(LongUriSerializer.instance().deserialize(uri));
        assertTrue(status.isFailure());
    }

    @Test
    @DisplayName("Test validate rpc method uri with version, when it is local but not an rpc method")
    void test_rpc_method_uri_with_version_when_it_is_not_valid_not_rpc_method_local() {
        final String uri = "/body.access//UpdateDoor";
        final ValidationResult status = UriValidator.validateRpcMethod(LongUriSerializer.instance().deserialize(uri));
        assertTrue(status.isFailure());
    }

    @Test
    @DisplayName("Test validate rpc method uri with version, when it is microRemote but not an rpc method")
    void test_rpc_method_uri_with_version_when_it_is_not_valid_not_rpc_method_remote() {
        final String uri = "//body.access/1/UpdateDoor";
        final ValidationResult status = UriValidator.validateRpcMethod(LongUriSerializer.instance().deserialize(uri));
        assertTrue(status.isFailure());
    }

    @Test
    @DisplayName("Test validate rpc method uri is invalid when uri is microRemote but missing authority")
    void test_rpc_method_uri_invalid_when_uri_is_remote_no_authority() {
        final String uri = "//";
        final ValidationResult status = UriValidator.validateRpcMethod(LongUriSerializer.instance().deserialize(uri));
        assertTrue(status.isFailure());
    }

    @Test
    @DisplayName("Test validate rpc method uri is invalid when uri is microRemote with use but missing authority")
    void test_rpc_method_uri_invalid_when_uri_is_remote_no_authority_with_use() {
        final String uri = "///body.access/1/rpc.UpdateDoor";
        final UUri uuri = LongUriSerializer.instance().deserialize(uri);
        final ValidationResult status = UriValidator.validateRpcMethod(uuri);
        assertEquals("", uuri.toString());
        assertTrue(status.isFailure());

    }

    @Test
    @DisplayName("Test validate rpc method uri is invalid when uri has authority but missing remote case")
    void test_rpc_method_uri_invalid_when_uri_is_remote_missing_authority_remotecase() {
        final UUri uuri =
                UUri.newBuilder().setEntity(UEntity.newBuilder().setName("body.access").build()).setResource(UResource.newBuilder().setName("rpc").setInstance("UpdateDoor").build()).setAuthority(UAuthority.newBuilder().build()).build();
        final ValidationResult status = UriValidator.validateRpcMethod(uuri);
        assertTrue(status.isFailure());
        assertEquals("Uri is remote missing uAuthority.", status.getMessage());

    }

    @Test
    @DisplayName("Test validate rpc method uri is invalid when uri has no use information")
    void test_rpc_method_uri_invalid_when_uri_is_missing_use() {
        final String uri = "//VCU.myvin";
        final ValidationResult status = UriValidator.validateRpcMethod(LongUriSerializer.instance().deserialize(uri));
        assertTrue(status.isFailure());
    }

    @Test
    @DisplayName("Test validate local rpc method uri is invalid when uri is missing use name")
    void test_rpc_method_uri_invalid_when_uri_is_missing_use_name_local() {
        final String uri = "/1/rpc.UpdateDoor";
        final ValidationResult status = UriValidator.validateRpcMethod(LongUriSerializer.instance().deserialize(uri));
        assertTrue(status.isFailure());
    }

    @Test
    @DisplayName("Test validate microRemote rpc method uri is invalid when uri is missing use name")
    void test_rpc_method_uri_invalid_when_uri_is_missing_use_name_remote() {
        final String uri = "//VCU.myvin//1/rpc.UpdateDoor";
        final ValidationResult status = UriValidator.validateRpcMethod(LongUriSerializer.instance().deserialize(uri));
        assertTrue(status.isFailure());
    }

    @Test
    @DisplayName("Test all valid uris from uris.json")
    public void test_all_valid_uris() throws IOException {
        // Access the "validUris" array
        JSONArray validUris = getJsonObject().getJSONArray("validUris");
        for (int i = 0; i < validUris.length(); i++) {
            UUri uuri = LongUriSerializer.instance().deserialize(validUris.getString(i));
            final ValidationResult status = UriValidator.validate(uuri);
            assertTrue(status.isSuccess());
        }
    }

    @Test
    @DisplayName("Test all invalid uris from uris.json")
    public void test_all_invalid_uris() throws IOException {
        // Access the "invalidUris" array
        JSONArray invalidUris = getJsonObject().getJSONArray("invalidUris");
        for (int i = 0; i < invalidUris.length(); i++) {
            JSONObject uriObject = invalidUris.getJSONObject(i);
            UUri uuri = LongUriSerializer.instance().deserialize(uriObject.getString("uri"));
            final ValidationResult status = UriValidator.validate(uuri);
            assertTrue(status.isFailure());
            assertEquals(status.getMessage(), uriObject.getString("status_message"));
        }
    }

    @Test
    @DisplayName("Test all valid rpc uris from uris.json")
    public void test_all_valid_rpc_uris() throws IOException {
        // Access the "validRpcUris" array
        JSONArray validRpcUris = getJsonObject().getJSONArray("validRpcUris");
        for (int i = 0; i < validRpcUris.length(); i++) {
            UUri uuri = LongUriSerializer.instance().deserialize(validRpcUris.getString(i));
            final ValidationResult status = UriValidator.validateRpcMethod(uuri);
            assertTrue(status.isSuccess());
        }
    }

    @Test
    @DisplayName("Test all invalid rpc uris from uris.json")
    public void test_all_invalid_rpc_uris() throws IOException {
        // Access the "invalidRpcUris" array
        JSONArray invalidRpcUris = getJsonObject().getJSONArray("invalidRpcUris");
        for (int i = 0; i < invalidRpcUris.length(); i++) {
            JSONObject uriObject = invalidRpcUris.getJSONObject(i);
            UUri uuri = LongUriSerializer.instance().deserialize(uriObject.getString("uri"));
            final ValidationResult status = UriValidator.validateRpcMethod(uuri);
            assertTrue(status.isFailure());
            assertEquals(status.getMessage(), uriObject.getString("status_message"));
        }
    }

    @Test
    @DisplayName("Test all valid rpc response uris from uris.json")
    public void test_all_valid_rpc_response_uris() throws IOException {
        // Access the "validRpcResponseUris" array
        JSONArray validRpcResponseUris = getJsonObject().getJSONArray("validRpcResponseUris");
        for (int i = 0; i < validRpcResponseUris.length(); i++) {
            UUri uuri = LongUriSerializer.instance().deserialize(validRpcResponseUris.getString(i));
            final ValidationResult status = UriValidator.validateRpcResponse(uuri);
            assertTrue(UriValidator.isRpcResponse(uuri));
            assertTrue(status.isSuccess());
        }
    }

    @Test
    @DisplayName("Test valid rpc response uri")
    public void test_valid_rpc_response_uri() throws IOException {
        UUri uuri =
                UUri.newBuilder()
                    .setEntity(UEntity.newBuilder().setName("hartley").build())
                    .setResource(UResourceBuilder.forRpcResponse()).build();
        final ValidationResult status = UriValidator.validateRpcResponse(uuri);
        assertTrue(UriValidator.isRpcResponse(uuri));
        assertTrue(status.isSuccess());
    }

    @Test
    @DisplayName("Test invalid rpc response uri")
    public void test_invalid_rpc_response_uri() throws IOException {
        UUri uuri =
                UUri.newBuilder()
                    .setEntity(UEntity.newBuilder().setName("hartley").build())
                    .setResource(UResource.newBuilder().setName("rpc").setId(19999).build()).build();
        final ValidationResult status = UriValidator.validateRpcResponse(uuri);
        assertFalse(UriValidator.isRpcResponse(uuri));
        assertFalse(status.isSuccess());
    }

    @Test
    @DisplayName("Test invalid rpc response uri")
    public void test_another_invalid_rpc_response_uri() throws IOException {
        UUri uuri =
                UUri.newBuilder()
                    .setEntity(UEntity.newBuilder().setName("hartley").build())
                    .setResource(UResource.newBuilder().setName("hello").setId(19999).build()).build();
        final ValidationResult status = UriValidator.validateRpcResponse(uuri);
        assertFalse(UriValidator.isRpcResponse(uuri));
        assertFalse(status.isSuccess());
    }


    @Test
    @DisplayName("Test all invalid rpc response uris from uris.json")
    public void test_all_invalid_rpc_response_uris() throws IOException {
        // Access the "invalidRpcResponseUris" array
        JSONArray invalidRpcResponseUris = getJsonObject().getJSONArray("invalidRpcResponseUris");
        for (int i = 0; i < invalidRpcResponseUris.length(); i++) {
            UUri uuri = LongUriSerializer.instance().deserialize(invalidRpcResponseUris.getString(i));
            final ValidationResult status = UriValidator.validateRpcResponse(uuri);
            assertTrue(status.isFailure());
        }
    }

    private JSONObject getJsonObject() throws IOException {
        String currentDirectory = System.getProperty("user.dir");
        String pkgname = this.getClass().getPackage().getName().replace(".", "/");
        File jsonFile = new File(currentDirectory,
                "src" + File.separator + "test" + File.separator + "java" + File.separator + pkgname + File.separator + "uris.json");

        // Open the file for reading
        BufferedReader reader = new BufferedReader(new FileReader(jsonFile));
        // Read the JSON data as a string
        StringBuilder jsonStringBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            jsonStringBuilder.append(line);
        }
        reader.close();
        // Parse the JSON data into a JSONObject
        return new JSONObject(jsonStringBuilder.toString());
    }

    @Test
    @DisplayName("Test is Remote is false for URI without UAuthority")
    public void test_is_remote_is_false_for_uri_without_uauthority() {
        UUri uri = UUri.newBuilder()
                .setAuthority(UAuthority.newBuilder().build())
                .setEntity(UEntity.newBuilder().setName("hartley").build())
                .setResource(UResourceBuilder.forRpcResponse())
                .build();
        assertFalse(UriValidator.isRemote(UAuthority.getDefaultInstance()));
        assertFalse(UriValidator.isRemote(uri.getAuthority()));
    }

    @Test
    @DisplayName("Test isRpcMethod with UResource and no UAuthority")
    public void test_is_rpc_method_with_uresource_and_no_uauthority() {

        assertFalse(UriValidator.isRpcMethod(UUri.newBuilder().build()));

        UUri uri = UUri.newBuilder()
        .setEntity(UEntity.newBuilder().setName("hartley").build())
        .setResource(UResourceBuilder.fromId(0x8000))
        .build();
        assertFalse(UriValidator.isRpcMethod(uri));
    }
    
    @Test
    @DisplayName("Test isRpcMethod passing null for uri")
    public void test_is_rpc_method_passing_null_for_uri() {
        assertFalse(UriValidator.isRpcMethod((UUri)null));
    }
    
    @Test
    @DisplayName("Test isRpcMethod passing null for resource")
    public void test_is_rpc_method_passing_null_for_resource() {
        assertFalse(UriValidator.isRpcMethod((UResource)null));
    }

    @Test
    @DisplayName("Test isRpcMethod for UResource without an instance")
    public void test_is_rpc_method_for_uresource_without_an_instance() {
        UResource resource = UResource.newBuilder().setName("rpc").build();
        assertFalse(UriValidator.isRpcMethod(resource));
    }

    @Test
    @DisplayName("Test isRpcMethod for UResource an empty instance")
    public void test_is_rpc_method_for_uresource_with_an_empty_instance() {
        UResource resource = UResource.newBuilder().setName("rpc").setInstance("").build();
        assertFalse(UriValidator.isRpcMethod(resource));
    }

    @Test
    @DisplayName("Test isRpcMethod for UResource with id that is less than min_topic")
    public void test_is_rpc_method_for_uresource_with_id_that_is_less_than_min_topic() {
        UResource resource = UResource.newBuilder().setName("rpc").setId(0).build();
        assertTrue(UriValidator.isRpcMethod(resource));
    }

    @Test
    @DisplayName("Test isRpcMethod for UResource with id that is greater than min_topic")
    public void test_is_rpc_method_for_uresource_with_id_that_is_greater_than_min_topic() {
        UResource resource = UResource.newBuilder().setName("rpc").setId(0x8000).build();
        assertFalse(UriValidator.isRpcMethod(resource));
    }


    @Test
    @DisplayName("Test isEmpty with null uri")
    public void test_is_empty_with_null_uri() {
        assertTrue(UriValidator.isEmpty(null));
    }

    @Test
    @DisplayName("Test isResolved when URI is long form only")
    public void test_is_resolved_when_uri_is_long_form_only() {
        UUri uri = UUri.newBuilder()
                .setEntity(UEntity.newBuilder().setName("hartley").setVersionMajor(23))
                .setResource(UResource.newBuilder().setName("rpc").setInstance("echo"))
                .build();
        assertFalse(UriValidator.isResolved(uri));
    }

    @Test
    @DisplayName("Test isResolved when URI is micro form only")
    public void test_is_resolved_when_uri_is_micro_form_only() {
        UUri uri = UUri.newBuilder()
                .setEntity(UEntity.newBuilder().setId(0).setVersionMajor(23))
                .setResource(UResourceBuilder.fromId(300))
                .build();
        assertFalse(UriValidator.isResolved(uri));
    }

    @Test
    @DisplayName("Test isResolved when URI is both long and micro form")
    public void test_is_resolved_when_uri_is_both_long_and_micro_form() {
        UUri uri = UUri.newBuilder()
                .setEntity(UEntity.newBuilder().setName("hartley").setId(0).setVersionMajor(23))
                .setResource(UResourceBuilder.forRpcResponse())
                .build();
        assertTrue(UriValidator.isResolved(uri));
    }

    @Test
    @DisplayName("Test isRpcResponse when URI is null")
    public void test_is_rpc_response_when_uri_is_null() {
        assertFalse(UriValidator.isRpcResponse((UUri)null));
    }

    @Test
    @DisplayName("Test isRpcResponse when uri is a valid RPC response")
    public void test_is_rpc_response_when_uri_is_a_valid_rpc_response() {
        UUri uri = UUri.newBuilder()
                .setEntity(UEntity.newBuilder().setName("hartley").setId(0).setVersionMajor(23))
                .setResource(UResourceBuilder.forRpcResponse())
                .build();
        assertTrue(UriValidator.isRpcResponse(uri));
    }

    @Test
    @DisplayName("Test isMicroForm when URI is null")
    public void test_is_micro_form_when_uri_is_null() {
        assertFalse(UriValidator.isMicroForm((UUri)null));
    }

    @Test
    @DisplayName("Test isMicroForm when URI is empty")
    public void test_is_micro_form_when_uri_is_empty() {
        assertFalse(UriValidator.isMicroForm(UUri.getDefaultInstance()));
    }

    @Test 
    @DisplayName("Test isMicroForm when URI does not have UResource but does have UEntity")
    public void test_is_micro_form_when_uri_does_not_have_uresource_but_does_have_uentity() {
        UUri uri = UUri.newBuilder()
                .setEntity(UEntity.newBuilder().setName("hartley").setId(0).setVersionMajor(23))
                .build();
        assertFalse(UriValidator.isMicroForm(uri));
    }

    @Test
    @DisplayName("Test isLongForm when URI is null")
    public void test_is_long_form_when_uri_is_null() {
        assertFalse(UriValidator.isLongForm((UUri)null));
        assertFalse(UriValidator.isLongForm((UAuthority)null));
    }

    @Test 
    @DisplayName("Test isLongForm when UAuthority is not long form")
    public void test_is_long_form_when_uauthority_is_not_long_form() {
        UUri uri = UUri.newBuilder()
                .setEntity(UEntity.newBuilder().setName("hartley").setId(0).setVersionMajor(23))
                .setAuthority(UAuthority.getDefaultInstance())
                .build();
        assertFalse(UriValidator.isLongForm(uri));
        assertTrue(UriValidator.isLongForm(uri.getAuthority()));
    }

    @Test
    @DisplayName("Test isLongForm when UAuthority is long form but not the rest")
    public void test_is_long_form_when_uauthority_is_long_form_but_not_the_rest() {
        UUri uri = UUri.newBuilder()
                .setEntity(UEntity.newBuilder().setName("hartley").setId(0).setVersionMajor(23))
                .setAuthority(UAuthority.newBuilder().setName("vcu.veh.gm.com").build())
                .build();
        assertFalse(UriValidator.isLongForm(uri));
        assertTrue(UriValidator.isLongForm(uri.getAuthority()));
    }

    @Test
    @DisplayName("Test isLongForm when UAuthority blank name")
    public void test_is_long_form_when_uauthority_is_blank_name() {
        UUri uri = UUri.newBuilder()
                .setEntity(UEntity.newBuilder().setName("hartley").setId(0).setVersionMajor(23))
                .setAuthority(UAuthority.newBuilder().setName("").build())
                .build();
        assertFalse(UriValidator.isLongForm(uri));
        assertFalse(UriValidator.isLongForm(uri.getAuthority()));
    }

    @Test
    @DisplayName("Test isLongForm when UAuthority is not long form but the rest is")
    public void test_is_long_form_when_uauthority_is_not_long_form_but_the_rest_is() {
        UUri uri = UUri.newBuilder()
                .setEntity(UEntity.newBuilder().setName("hartley").setId(0).setVersionMajor(23))
                .setResource(UResourceBuilder.forRpcResponse())
                .setAuthority(UAuthority.newBuilder().setId(ByteString.copyFromUtf8("hello Jello")))
                .build();
        assertFalse(UriValidator.isLongForm(uri));
        assertFalse(UriValidator.isLongForm(uri.getAuthority()));
    }

    @Test
    @DisplayName("Test isLocal when authority is null")
    public void test_is_local_when_authority_is_null() {
        assertFalse(UriValidator.isLocal((UAuthority)null));
    }

    @Test
    @DisplayName("Test isRemote when authority is null")
    public void test_is_remote_when_authority_is_null() {
        assertFalse(UriValidator.isRemote((UAuthority)null));
    }

    @Test
    @DisplayName("Test isRemote when authority doesn't have a name but does have a number set")
    public void test_is_remote_when_authority_does_not_have_a_name_but_does_have_a_number_set() {
        UAuthority authority = UAuthority.newBuilder().setId(ByteString.copyFromUtf8("hello Jello")).build();
        assertTrue(UriValidator.isRemote(authority));
        assertFalse(authority.hasName());
        assertEquals(authority.getNumberCase(), UAuthority.NumberCase.ID);
    }
    
    @Test
    @DisplayName("Test isRemote when authority has name and number set")
    public void test_is_remote_when_authority_has_name_and_number_set() {
        UAuthority authority = UAuthority.newBuilder().setName("vcu.veh.gm.com").setId(ByteString.copyFromUtf8("hello Jello")).build();
        assertTrue(UriValidator.isRemote(authority));
        assertTrue(authority.hasName());
        assertEquals(authority.getNumberCase(), UAuthority.NumberCase.ID);
    }

    @Test
    @DisplayName("Test isRemote when authority has name and number is NOT set")
    public void test_is_remote_when_authority_has_name_and_number_is_not_set() {
        UAuthority authority = UAuthority.newBuilder().setName("vcu.veh.gm.com").build();
        assertTrue(UriValidator.isRemote(authority));
        assertTrue(authority.hasName());
        assertEquals(authority.getNumberCase(), UAuthority.NumberCase.NUMBER_NOT_SET);
    }
    
}
