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

package org.eclipse.uprotocol.uri.validator;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.uprotocol.uri.serializer.LongUriSerializer;
import org.eclipse.uprotocol.v1.UAuthority;
import org.eclipse.uprotocol.v1.UEntity;
import org.eclipse.uprotocol.v1.UResource;
import org.eclipse.uprotocol.v1.UUri;
import org.eclipse.uprotocol.validation.ValidationResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    @DisplayName("Test validateRpcMethod with valid URI")
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
        final UUri uuri = UUri.newBuilder().setEntity(UEntity.newBuilder().setName("body.access").build()).setResource(UResource.newBuilder().setName("rpc").setInstance("UpdateDoor").build()).setAuthority(UAuthority.newBuilder().build()).build();
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
        List<String> validUris = (List<String>) getJsonMap().get("validUris");

        for (String uri : validUris) {
            UUri uuri = LongUriSerializer.instance().deserialize(uri);
            final ValidationResult status = UriValidator.validate(uuri);
            assertTrue(status.isSuccess());
        }

    }
    @Test
    @DisplayName("Test all invalid uris from uris.json")
    public void test_all_invalid_uris() throws IOException {
//
        // Access the "invalidUris" array
        List<LinkedHashMap<String, String>> invalidUris = (List<LinkedHashMap<String, String>>) getJsonMap().get("invalidUris");

        for (LinkedHashMap<String, String> map : invalidUris) {
            UUri uuri = LongUriSerializer.instance().deserialize(map.get("uri"));
            final ValidationResult status = UriValidator.validate(uuri);
            assertTrue(status.isFailure());
            assertEquals(status.getMessage(),map.get("reason"));
        }

    }
    @Test
    @DisplayName("Test all valid rpc uris from uris.json")
    public void test_all_valid_rpc_uris() throws IOException {

        // Access the "validUris" array
        List<String> validUris = (List<String>) getJsonMap().get("validRpcUris");

        for (String uri : validUris) {
            UUri uuri = LongUriSerializer.instance().deserialize(uri);
            final ValidationResult status = UriValidator.validateRpcMethod(uuri);
            assertTrue(status.isSuccess());
        }

    }
    @Test
    @DisplayName("Test all invalid rpc uris from uris.json")
    public void test_all_invalid_rpc_uris() throws IOException {

        // Access the "validUris" array
        List<LinkedHashMap<String, String>> invalidRpcUris = (List<LinkedHashMap<String, String>>)  getJsonMap().get(
                "invalidRpcUris");

        for (LinkedHashMap<String, String> map : invalidRpcUris) {
            UUri uuri = LongUriSerializer.instance().deserialize(map.get("uri"));
            final ValidationResult status = UriValidator.validateRpcMethod(uuri);
            assertTrue(status.isFailure());
            assertEquals(status.getMessage(),map.get("status_message"));
        }

    }
    @Test
    @DisplayName("Test all valid rpc response uris from uris.json")
    public void test_all_valid_rpc_response_uris() throws IOException {

        // Access the "validUris" array
        List<String> validUris = (List<String>) getJsonMap().get("validRpcResponseUris");

        for (String uri : validUris) {
            UUri uuri = LongUriSerializer.instance().deserialize(uri);
            final ValidationResult status = UriValidator.validateRpcResponse(uuri);
            assertTrue(status.isSuccess());
        }

    }
    @Test
    @DisplayName("Test all invalid rpc response uris from uris.json")
    public void test_all_invalid_rpc_response_uris() throws IOException {

        // Access the "validUris" array
        List<String> inValidUris = (List<String>) getJsonMap().get("invalidRpcResponseUris");

        for (String uri : inValidUris) {
            UUri uuri = LongUriSerializer.instance().deserialize(uri);
            final ValidationResult status = UriValidator.validateRpcResponse(uuri);
            assertTrue(status.isFailure());
        }

    }
    private Map<String, ?> getJsonMap() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        String currentDirectory = System.getProperty("user.dir");
        String pkgname = this.getClass().getPackage().getName().replace(".", "/");
        File jsonFile = new File(currentDirectory, "src" + File.separator + "test" + File.separator + "java" + File.separator + pkgname + File.separator + "uris.json");
        // Read JSON data from the file as a Map
        Map<String,?> jsonMap = objectMapper.readValue(jsonFile, Map.class);
        return jsonMap;
    }

}
