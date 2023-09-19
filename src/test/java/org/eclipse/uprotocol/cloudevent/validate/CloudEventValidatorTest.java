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

package org.eclipse.uprotocol.cloudevent.validate;

import org.eclipse.uprotocol.cloudevent.datamodel.UCloudEventAttributes;
import org.eclipse.uprotocol.cloudevent.datamodel.UCloudEventType;
import org.eclipse.uprotocol.cloudevent.factory.CloudEventFactory;
import org.eclipse.uprotocol.uri.datamodel.UAuthority;
import org.eclipse.uprotocol.uri.datamodel.UEntity;
import org.eclipse.uprotocol.uri.datamodel.UResource;
import org.eclipse.uprotocol.uri.datamodel.UUri;
import org.eclipse.uprotocol.uri.serializer.UriSerializer;
import org.eclipse.uprotocol.uuid.factory.UUIDFactory;
import com.google.protobuf.Any;
import com.google.rpc.Code;
import com.google.rpc.Status;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CloudEventValidatorTest {

    @Test
    @DisplayName("Test get a publish cloud event validator")
    void test_get_a_publish_cloud_event_validator() {
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest()
                .withType("pub.v1");
        CloudEvent cloudEvent = builder.build();
        final CloudEventValidator validator = CloudEventValidator.getValidator(cloudEvent);
        final Status status = validator.validateType(cloudEvent).toStatus();
        assertEquals(status, ValidationResult.STATUS_SUCCESS);
        assertEquals("CloudEventValidator.Publish", validator.toString());
    }

    @Test
    @DisplayName("Test get a notification cloud event validator")
    void test_get_a_notification_cloud_event_validator() {
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest()
                .withExtension("sink", "//bo.cloud/petapp")
                .withType("pub.v1");
        CloudEvent cloudEvent = builder.build();
        final CloudEventValidator validator = CloudEventValidator.Validators.NOTIFICATION.validator();
        final Status status = validator.validateType(cloudEvent).toStatus();
        assertEquals(status, ValidationResult.STATUS_SUCCESS);
        assertEquals("CloudEventValidator.Notification", validator.toString());
    }

    @Test
    @DisplayName("Test publish cloud event type")
    void test_publish_cloud_event_type() {
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest()
                .withType("res.v1");
        CloudEvent cloudEvent = builder.build();
        final CloudEventValidator validator = CloudEventValidator.Validators.PUBLISH.validator();
        final Status status = validator.validateType(cloudEvent).toStatus();
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status.getCode());
        assertEquals("Invalid CloudEvent type [res.v1]. CloudEvent of type Publish must have a type of 'pub.v1'", status.getMessage());
    }

    @Test
    @DisplayName("Test notification cloud event type")
    void test_notification_cloud_event_type() {
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest()
                .withType("res.v1");
        CloudEvent cloudEvent = builder.build();
        final CloudEventValidator validator = CloudEventValidator.Validators.NOTIFICATION.validator();
        final Status status = validator.validateType(cloudEvent).toStatus();
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status.getCode());
        assertEquals("Invalid CloudEvent type [res.v1]. CloudEvent of type Publish must have a type of 'pub.v1'", status.getMessage());
    }


    @Test
    @DisplayName("Test get a request cloud event validator")
    void test_get_a_request_cloud_event_validator() {
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest()
                .withType("req.v1");
        CloudEvent cloudEvent = builder.build();
        final CloudEventValidator validator = CloudEventValidator.getValidator(cloudEvent);
        final Status status = validator.validateType(cloudEvent).toStatus();
        assertEquals(status, ValidationResult.STATUS_SUCCESS);
        assertEquals("CloudEventValidator.Request", validator.toString());
    }

    @Test
    @DisplayName("Test request cloud event type")
    void test_request_cloud_event_type() {
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest()
                .withType("pub.v1");
        CloudEvent cloudEvent = builder.build();
        final CloudEventValidator validator = CloudEventValidator.Validators.REQUEST.validator();
        final Status status = validator.validateType(cloudEvent).toStatus();
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status.getCode());
        assertEquals("Invalid CloudEvent type [pub.v1]. CloudEvent of type Request must have a type of 'req.v1'", status.getMessage());
    }

    @Test
    @DisplayName("Test get a response cloud event validator")
    void test_get_a_response_cloud_event_validator() {
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest()
                .withType("res.v1");
        CloudEvent cloudEvent = builder.build();
        final CloudEventValidator validator = CloudEventValidator.getValidator(cloudEvent);
        final Status status = validator.validateType(cloudEvent).toStatus();
        assertEquals(status, ValidationResult.STATUS_SUCCESS);
        assertEquals("CloudEventValidator.Response", validator.toString());
    }

    @Test
    @DisplayName("Test response cloud event type")
    void test_response_cloud_event_type() {
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest()
                .withType("pub.v1");
        CloudEvent cloudEvent = builder.build();
        final CloudEventValidator validator = CloudEventValidator.Validators.RESPONSE.validator();
        final Status status = validator.validateType(cloudEvent).toStatus();
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status.getCode());
        assertEquals("Invalid CloudEvent type [pub.v1]. CloudEvent of type Response must have a type of 'res.v1'", status.getMessage());
    }

    @Test
    @DisplayName("Test get a publish cloud event validator when cloud event type is unknown")
    void test_get_a_publish_cloud_event_validator_when_cloud_event_type_is_unknown() {
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest()
                .withType("lala.v1");
        CloudEvent cloudEvent = builder.build();
        final CloudEventValidator validator = CloudEventValidator.getValidator(cloudEvent);
        assertEquals("CloudEventValidator.Publish", validator.toString());
    }

    @Test
    @DisplayName("Test validate version")
    void validate_cloud_event_version_when_valid() {
        UUID uuid = UUIDFactory.Factories.UPROTOCOL.factory().create();
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest()
                .withType(UCloudEventType.PUBLISH.type())
                .withId(uuid.toString());
        CloudEvent cloudEvent = builder.build();
        final Status status = CloudEventValidator.validateVersion(cloudEvent).toStatus();

        assertEquals(status, ValidationResult.STATUS_SUCCESS);
    }

    @Test
    @DisplayName("Test validate version when not valid")
    void validate_cloud_event_version_when_not_valid() {
        final Any payloadForTest = buildProtoPayloadForTest();
        final CloudEventBuilder builder = CloudEventBuilder.v03()
                .withId("id")
                .withType("pub.v1")
                .withSource(URI.create("/body.access"))
                .withDataContentType("application/protobuf")
                .withDataSchema(URI.create(payloadForTest.getTypeUrl()))
                .withData(payloadForTest.toByteArray());

        CloudEvent cloudEvent = builder.build();
        final Status status = CloudEventValidator.validateVersion(cloudEvent).toStatus();
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status.getCode());
        assertEquals("Invalid CloudEvent version [0.3]. CloudEvent version must be 1.0.", status.getMessage());
    }

    @Test
    @DisplayName("Test validate cloudevent id when valid")
    void validate_cloud_event_id_when_valid() {
        UUID uuid = UUIDFactory.Factories.UPROTOCOL.factory().create();
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest()
                .withType(UCloudEventType.PUBLISH.type())
                .withId(uuid.toString());
        CloudEvent cloudEvent = builder.build();
        final Status status = CloudEventValidator.validateId(cloudEvent).toStatus();

        assertEquals(status, ValidationResult.STATUS_SUCCESS);
    }

    @Test
    @DisplayName("Test validate cloudevent id when not UUIDv8 type id")
    void validate_cloud_event_id_when_not_uuidv6_type_id() {
        UUID uuid = UUID.randomUUID();
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest()
                .withType(UCloudEventType.PUBLISH.type())
                .withId(uuid.toString());
        CloudEvent cloudEvent = builder.build();
        final Status status = CloudEventValidator.validateId(cloudEvent).toStatus();
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status.getCode());
        assertEquals("Invalid CloudEvent Id ["+ uuid +"]. CloudEvent Id must be of type UUIDv8.", status.getMessage());
    }

    @Test
    @DisplayName("Test validate cloudevent id when not valid")
    void validate_cloud_event_id_when_not_valid() {
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest()
                .withType(UCloudEventType.PUBLISH.type())
                .withId("testme");
        CloudEvent cloudEvent = builder.build();
        final Status status = CloudEventValidator.validateId(cloudEvent).toStatus();
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status.getCode());
        assertEquals("Invalid CloudEvent Id [testme]. CloudEvent Id must be of type UUIDv8.", status.getMessage());
    }

    @Test
    @DisplayName("Test validate software entity uri with version, when it is valid microRemote")
    void test_usoftware_entity_uri_with_version_when_it_is_valid_remote() {

        final String uri = "//VCU.MY_CAR_VIN/body.access/1";

        final Status status = CloudEventValidator.validateUEntityUri(uri).toStatus();
        assertEquals(ValidationResult.STATUS_SUCCESS, status);
    }

    @Test
    @DisplayName("Test validate software entity uri no version, when it is valid microRemote")
    void test_usoftware_entity_uri_no_version_when_it_is_valid_remote() {

        final String uri = "//VCU.MY_CAR_VIN/body.access";

        final Status status = CloudEventValidator.validateUEntityUri(uri).toStatus();
        assertEquals(ValidationResult.STATUS_SUCCESS, status);
    }

    @Test
    @DisplayName("Test validate software entity uri with version, when it is valid local")
    void test_usoftware_entity_uri_with_version_when_it_is_valid_local() {

        final String uri = "/body.access/1";

        final Status status = CloudEventValidator.validateUEntityUri(uri).toStatus();
        assertEquals(ValidationResult.STATUS_SUCCESS, status);
    }

    @Test
    @DisplayName("Test validate software entity uri no version, when it is valid local")
    void test_usoftware_entity_uri_no_version_when_it_is_valid_local() {

        final String uri = "/body.access/";

        final Status status = CloudEventValidator.validateUEntityUri(uri).toStatus();
        assertEquals(ValidationResult.STATUS_SUCCESS, status);
    }

    @Test
    @DisplayName("Test validate software entity uri is invalid when uri contains nothing but schema")
    void test_usoftware_entity_uri_invalid_when_uri_has_schema_only() {

        final String uri = ":";

        final Status status = CloudEventValidator.validateUEntityUri(uri).toStatus();
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status.getCode());
        assertEquals("UriPart is missing uSoftware Entity name.", status.getMessage());
    }

    @Test
    @DisplayName("Test validate software entity uri is invalid when uri is microRemote but missing authority")
    void test_usoftware_entity_uri_invalid_when_uri_is_remote_no_authority() {

        final String uri = "//";

        final Status status = CloudEventValidator.validateUEntityUri(uri).toStatus();
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status.getCode());
        assertEquals("UriPart is configured to be microRemote and is missing uAuthority device name.", status.getMessage());
    }

    @Test
    @DisplayName("Test validate software entity uri is invalid when uri is microRemote with use but missing authority")
    void test_usoftware_entity_uri_invalid_when_uri_is_remote_no_authority_with_use() {

        final String uri = "///body.access/1";

        final Status status = CloudEventValidator.validateUEntityUri(uri).toStatus();
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status.getCode());
        assertEquals("UriPart is configured to be microRemote and is missing uAuthority device name.", status.getMessage());
    }

    @Test
    @DisplayName("Test validate software entity uri is invalid when uri has no use information")
    void test_usoftware_entity_uri_invalid_when_uri_is_missing_use() {

        final String uri = "//VCU.myvin";

        final Status status = CloudEventValidator.validateUEntityUri(uri).toStatus();
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status.getCode());
        assertEquals("UriPart is missing uSoftware Entity name.", status.getMessage());
    }

    @Test
    @DisplayName("Test validate local software entity uri is invalid when uri is missing use name")
    void test_usoftware_entity_uri_invalid_when_uri_is_missing_use_name_local() {

        final String uri = "//VCU.myvin//1";

        final Status status = CloudEventValidator.validateUEntityUri(uri).toStatus();
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status.getCode());
        assertEquals("UriPart is missing uSoftware Entity name.", status.getMessage());
    }

    @Test
    @DisplayName("Test validate topic uri with version, when it is valid microRemote")
    void test_topic_uri_with_version_when_it_is_valid_remote() {

        final String uri = "//VCU.MY_CAR_VIN/body.access/1/door.front_left#Door";

        final Status status = CloudEventValidator.validateTopicUri(uri).toStatus();
        assertEquals(ValidationResult.STATUS_SUCCESS, status);
    }

    @Test
    @DisplayName("Test validate topic uri no version, when it is valid microRemote")
    void test_topic_uri_no_version_when_it_is_valid_remote() {

        final String uri = "//VCU.MY_CAR_VIN/body.access//door.front_left#Door";

        final Status status = CloudEventValidator.validateTopicUri(uri).toStatus();
        assertEquals(ValidationResult.STATUS_SUCCESS, status);
    }

    @Test
    @DisplayName("Test validate topic uri with version, when it is valid local")
    void test_topic_uri_with_version_when_it_is_valid_local() {

        final String uri = "/body.access/1/door.front_left#Door";

        final Status status = CloudEventValidator.validateTopicUri(uri).toStatus();
        assertEquals(ValidationResult.STATUS_SUCCESS, status);
    }

    @Test
    @DisplayName("Test validate topic uri no version, when it is valid local")
    void test_topic_uri_no_version_when_it_is_valid_local() {

        final String uri = "/body.access//door.front_left#Door";

        final Status status = CloudEventValidator.validateTopicUri(uri).toStatus();
        assertEquals(ValidationResult.STATUS_SUCCESS, status);
    }

    @Test
    @DisplayName("Test validate topic uri is invalid when uri contains nothing but schema")
    void test_topic_uri_invalid_when_uri_has_schema_only() {

        final String uri = ":";

        final Status status = CloudEventValidator.validateTopicUri(uri).toStatus();
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status.getCode());
        assertEquals("UriPart is missing uSoftware Entity name.", status.getMessage());
    }

    @Test
    @DisplayName("Test validate topic uri is invalid when uri contains empty use name local")
    void test_topic_uri_invalid_when_uri_has_empty_use_name_local() {

        final String uri = "/";

        final Status status = CloudEventValidator.validateTopicUri(uri).toStatus();
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status.getCode());
        assertEquals("UriPart is missing uSoftware Entity name.", status.getMessage());
    }

    @Test
    @DisplayName("Test validate topic uri is invalid when uri is microRemote but missing authority")
    void test_topic_uri_invalid_when_uri_is_remote_no_authority() {

        final String uri = "//";

        final Status status = CloudEventValidator.validateTopicUri(uri).toStatus();
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status.getCode());
        assertEquals("UriPart is configured to be microRemote and is missing uAuthority device name.", status.getMessage());
    }

    @Test
    @DisplayName("Test validate topic uri is invalid when uri is microRemote with use but missing authority")
    void test_topic_uri_invalid_when_uri_is_remote_no_authority_with_use() {

        final String uri = "///body.access/1/door.front_left#Door";

        final Status status = CloudEventValidator.validateTopicUri(uri).toStatus();
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status.getCode());
        assertEquals("UriPart is configured to be microRemote and is missing uAuthority device name.", status.getMessage());
    }

    @Test
    @DisplayName("Test validate topic uri is invalid when uri has no use information")
    void test_topic_uri_invalid_when_uri_is_missing_use_remote() {

        final String uri = "//VCU.myvin///door.front_left#Door";

        final Status status = CloudEventValidator.validateTopicUri(uri).toStatus();
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status.getCode());
        assertEquals("UriPart is missing uSoftware Entity name.", status.getMessage());
    }

    @Test
    @DisplayName("Test validate microRemote topic uri is invalid when uri is missing use name")
    void test_topic_uri_invalid_when_uri_is_missing_use_name_remote() {

        final String uri = "/1/door.front_left#Door";

        final Status status = CloudEventValidator.validateTopicUri(uri).toStatus();
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status.getCode());
        assertEquals("UriPart is missing uResource name.", status.getMessage());
    }

    @Test
    @DisplayName("Test validate local topic uri is invalid when uri is missing use name")
    void test_topic_uri_invalid_when_uri_is_missing_use_name_local() {

        final String uri = "//VCU.myvin//1";

        final Status status = CloudEventValidator.validateTopicUri(uri).toStatus();
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status.getCode());
        assertEquals("UriPart is missing uSoftware Entity name.", status.getMessage());
    }

    @Test
    @DisplayName("Test validate microRemote topic uri, when uri has authority and use no version missing resource")
    void test_topic_uri_when_uri_is_with_authority_with_use_no_version_missing_resource_remote() {

        final String source = "//VCU.myvin/body.access";

        final Status status = CloudEventValidator.validateTopicUri(source).toStatus();
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status.getCode());
        assertEquals("UriPart is missing uResource name.", status.getMessage());
    }

    @Test
    @DisplayName("Test validate microRemote topic uri, when uri has authority and use with version missing resource")
    void test_topic_uri_when_uri_is_with_authority_with_use_with_version_missing_resource_remote() {

        final String source = "//VCU.myvin/body.access/";

        final Status status = CloudEventValidator.validateTopicUri(source).toStatus();
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status.getCode());
        assertEquals("UriPart is missing uResource name.", status.getMessage());
    }

    @Test
    @DisplayName("Test validate microRemote topic uri, when uri has authority and use and resource missing Message")
    void test_topic_uri_when_uri_is_with_authority_with_use_with_resource_missing_message_remote() {

        final String source = "//VCU.myvin/body.access/1/door.front_left";

        final Status status = CloudEventValidator.validateTopicUri(source).toStatus();
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status.getCode());
        assertEquals("UriPart is missing Message information.", status.getMessage());
    }

    @Test
    @DisplayName("Test validate rpc topic uri with version, when it is valid microRemote")
    void test_rpc_topic_uri_with_version_when_it_is_valid_remote() {

        final String uri = "//bo.cloud/petapp/1/rpc.response";

        final Status status = CloudEventValidator.validateRpcTopicUri(uri).toStatus();
        assertEquals(ValidationResult.STATUS_SUCCESS, status);
    }

    @Test
    @DisplayName("Test validate rpc topic uri no version, when it is valid microRemote")
    void test_rpc_topic_uri_no_version_when_it_is_valid_remote() {

        final String uri = "//bo.cloud/petapp//rpc.response";

        final Status status = CloudEventValidator.validateRpcTopicUri(uri).toStatus();
        assertEquals(ValidationResult.STATUS_SUCCESS, status);
    }

    @Test
    @DisplayName("Test validate rpc topic uri with version, when it is valid local")
    void test_rpc_topic_uri_with_version_when_it_is_valid_local() {

        final String uri = "/petapp/1/rpc.response";

        final Status status = CloudEventValidator.validateRpcTopicUri(uri).toStatus();
        assertEquals(ValidationResult.STATUS_SUCCESS, status);
    }

    @Test
    @DisplayName("Test validate rpc topic uri no version, when it is valid local")
    void test_rpc_topic_uri_no_version_when_it_is_valid_local() {

        final String uri = "/petapp//rpc.response";

        final Status status = CloudEventValidator.validateRpcTopicUri(uri).toStatus();
        assertEquals(ValidationResult.STATUS_SUCCESS, status);
    }

    @Test
    @DisplayName("Test validate rpc topic uri is invalid when uri contains nothing but schema")
    void test_rpc_topic_uri_invalid_when_uri_has_schema_only() {

        final String uri = ":";

        final Status status = CloudEventValidator.validateRpcTopicUri(uri).toStatus();
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status.getCode());
        assertEquals("Invalid RPC uri application response topic. UriPart is missing uSoftware Entity name.", status.getMessage());
    }

    @Test
    @DisplayName("Test validate rpc topic uri with version, when it is local but missing rpc.respons")
    void test_rpc_topic_uri_with_version_when_it_is_not_valid_missing_rpc_response_local() {

        final String uri = "/petapp/1/dog";

        final Status status = CloudEventValidator.validateRpcTopicUri(uri).toStatus();
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status.getCode());
        assertEquals("Invalid RPC uri application response topic. UriPart is missing rpc.response.", status.getMessage());
    }

    @Test
    @DisplayName("Test validate rpc topic uri with version, when it is microRemote but missing rpc.respons")
    void test_rpc_topic_uri_with_version_when_it_is_not_valid_missing_rpc_response_remote() {

        final String uri = "//petapp/1/dog";

        final Status status = CloudEventValidator.validateRpcTopicUri(uri).toStatus();
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status.getCode());
        assertEquals("Invalid RPC uri application response topic. UriPart is missing rpc.response.", status.getMessage());
    }

    @Test
    @DisplayName("Test validate rpc topic uri is invalid when uri is microRemote but missing authority")
    void test_rpc_topic_uri_invalid_when_uri_is_remote_no_authority() {

        final String uri = "//";

        final Status status = CloudEventValidator.validateRpcTopicUri(uri).toStatus();
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status.getCode());
        assertEquals("Invalid RPC uri application response topic. UriPart is configured to be microRemote and is missing uAuthority device name.", status.getMessage());
    }

    @Test
    @DisplayName("Test validate rpc topic uri is invalid when uri is microRemote with use but missing authority")
    void test_rpc_topic_uri_invalid_when_uri_is_remote_no_authority_with_use() {

        final String uri = "///body.access/1";

        final Status status = CloudEventValidator.validateRpcTopicUri(uri).toStatus();
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status.getCode());
        assertEquals("Invalid RPC uri application response topic. UriPart is configured to be microRemote and is missing uAuthority device name.", status.getMessage());
    }

    @Test
    @DisplayName("Test validate rpc topic uri is invalid when uri has no use information")
    void test_rpc_topic_uri_invalid_when_uri_is_missing_use() {

        final String uri = "//VCU.myvin";

        final Status status = CloudEventValidator.validateRpcTopicUri(uri).toStatus();
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status.getCode());
        assertEquals("Invalid RPC uri application response topic. UriPart is missing uSoftware Entity name.", status.getMessage());
    }

    @Test
    @DisplayName("Test validate microRemote rpc topic uri is invalid when uri is missing use name")
    void test_rpc_topic_uri_invalid_when_uri_is_missing_use_name_remote() {

        final String uri = "/1";

        final Status status = CloudEventValidator.validateRpcTopicUri(uri).toStatus();
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status.getCode());
        assertEquals("Invalid RPC uri application response topic. UriPart is missing rpc.response.", status.getMessage());
    }

    @Test
    @DisplayName("Test validate local rpc topic uri is invalid when uri is missing use name")
    void test_rpc_topic_uri_invalid_when_uri_is_missing_use_name_local() {

        final String uri = "//VCU.myvin//1";

        final Status status = CloudEventValidator.validateRpcTopicUri(uri).toStatus();
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status.getCode());
        assertEquals("Invalid RPC uri application response topic. UriPart is missing uSoftware Entity name.", status.getMessage());
    }

    @Test
    @DisplayName("Test validate rpc topic  uri with version, when it is valid")
    void test_rpc_topic__uri_with_version_when_it_is_valid() {

        UEntity use = UEntity.longFormat("petapp", 1);
        UAuthority uAuthority = UAuthority.longRemote("bo", "cloud");
        UResource uResource = UResource.forRpcResponse();
        UUri Uri = new UUri(uAuthority, use, uResource);

        final Status status = CloudEventValidator.validateRpcTopicUri(Uri).toStatus();
        assertEquals(ValidationResult.STATUS_SUCCESS, status);
    }

    @Test
    @DisplayName("Test validate rpc topic  uri with version, when it is not valid")
    void test_rpc_topic__uri_with_version_when_it_is_not_valid() {

        UEntity use = UEntity.longFormat("petapp", 1);
        UAuthority uAuthority = UAuthority.longRemote("bo", "cloud");
        UResource uResource = UResource.longFormat("body.access", "front_left", null);
        UUri Uri = new UUri(uAuthority, use, uResource);

        final Status status = CloudEventValidator.validateRpcTopicUri(Uri).toStatus();
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status.getCode());
        assertEquals("Invalid RPC uri application response topic. UriPart is missing rpc.response.", status.getMessage());
    }

    @Test
    @DisplayName("Test validate rpc method uri with version, when it is valid microRemote")
    void test_rpc_method_uri_with_version_when_it_is_valid_remote() {

        final String uri = "//VCU.myvin/body.access/1/rpc.UpdateDoor";

        final Status status = CloudEventValidator.validateRpcMethod(uri).toStatus();
        assertEquals(ValidationResult.STATUS_SUCCESS, status);
    }

    @Test
    @DisplayName("Test validate rpc method uri no version, when it is valid microRemote")
    void test_rpc_method_uri_no_version_when_it_is_valid_remote() {

        final String uri = "//VCU.myvin/body.access//rpc.UpdateDoor";

        final Status status = CloudEventValidator.validateRpcMethod(uri).toStatus();
        assertEquals(ValidationResult.STATUS_SUCCESS, status);
    }

    @Test
    @DisplayName("Test validate rpc method uri with version, when it is valid local")
    void test_rpc_method_uri_with_version_when_it_is_valid_local() {

        final String uri = "/body.access/1/rpc.UpdateDoor";

        final Status status = CloudEventValidator.validateRpcMethod(uri).toStatus();
        assertEquals(ValidationResult.STATUS_SUCCESS, status);
    }

    @Test
    @DisplayName("Test validate rpc method uri no version, when it is valid local")
    void test_rpc_method_uri_no_version_when_it_is_valid_local() {

        final String uri = "/body.access//rpc.UpdateDoor";

        final Status status = CloudEventValidator.validateRpcMethod(uri).toStatus();
        assertEquals(ValidationResult.STATUS_SUCCESS, status);
    }

    @Test
    @DisplayName("Test validate rpc method uri is invalid when uri contains nothing but schema")
    void test_rpc_method_uri_invalid_when_uri_has_schema_only() {

        final String uri = ":";

        final Status status = CloudEventValidator.validateRpcMethod(uri).toStatus();
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status.getCode());
        assertEquals("Invalid RPC method uri. UriPart is missing uSoftware Entity name.", status.getMessage());
    }

    @Test
    @DisplayName("Test validate rpc method uri with version, when it is local but not an rpc method")
    void test_rpc_method_uri_with_version_when_it_is_not_valid_not_rpc_method_local() {

        final String uri = "/body.access//UpdateDoor";

        final Status status = CloudEventValidator.validateRpcMethod(uri).toStatus();
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status.getCode());
        assertEquals("Invalid RPC method uri. UriPart should be the method to be called, or method from response.", status.getMessage());
    }

    @Test
    @DisplayName("Test validate rpc method uri with version, when it is microRemote but not an rpc method")
    void test_rpc_method_uri_with_version_when_it_is_not_valid_not_rpc_method_remote() {

        final String uri = "//body.access/1/UpdateDoor";

        final Status status = CloudEventValidator.validateRpcMethod(uri).toStatus();
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status.getCode());
        assertEquals("Invalid RPC method uri. UriPart should be the method to be called, or method from response.", status.getMessage());
    }

    @Test
    @DisplayName("Test validate rpc method uri is invalid when uri is microRemote but missing authority")
    void test_rpc_method_uri_invalid_when_uri_is_remote_no_authority() {

        final String uri = "//";

        final Status status = CloudEventValidator.validateRpcMethod(uri).toStatus();
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status.getCode());
        assertEquals("Invalid RPC method uri. UriPart is configured to be microRemote and is missing uAuthority device name.", status.getMessage());
    }

    @Test
    @DisplayName("Test validate rpc method uri is invalid when uri is microRemote with use but missing authority")
    void test_rpc_method_uri_invalid_when_uri_is_remote_no_authority_with_use() {

        final String uri = "///body.access/1/rpc.UpdateDoor";

        final Status status = CloudEventValidator.validateRpcMethod(uri).toStatus();
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status.getCode());
        assertEquals("Invalid RPC method uri. UriPart is configured to be microRemote and is missing uAuthority device name.", status.getMessage());
    }

    @Test
    @DisplayName("Test validate rpc method uri is invalid when uri has no use information")
    void test_rpc_method_uri_invalid_when_uri_is_missing_use() {

        final String uri = "//VCU.myvin";

        final Status status = CloudEventValidator.validateRpcMethod(uri).toStatus();
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status.getCode());
        assertEquals("Invalid RPC method uri. UriPart is missing uSoftware Entity name.", status.getMessage());
    }

    @Test
    @DisplayName("Test validate local rpc method uri is invalid when uri is missing use name")
    void test_rpc_method_uri_invalid_when_uri_is_missing_use_name_local() {

        final String uri = "/1/rpc.UpdateDoor";

        final Status status = CloudEventValidator.validateRpcMethod(uri).toStatus();
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status.getCode());
        assertEquals("Invalid RPC method uri. UriPart should be the method to be called, or method from response.", status.getMessage());
    }

    @Test
    @DisplayName("Test validate microRemote rpc method uri is invalid when uri is missing use name")
    void test_rpc_method_uri_invalid_when_uri_is_missing_use_name_remote() {

        final String uri = "//VCU.myvin//1/rpc.UpdateDoor";

        final Status status = CloudEventValidator.validateRpcMethod(uri).toStatus();
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status.getCode());
        assertEquals("Invalid RPC method uri. UriPart is missing uSoftware Entity name.", status.getMessage());
    }

    @Test
    @DisplayName("Test local Publish type CloudEvent is valid everything is valid")
    void test_publish_type_cloudevent_is_valid_when_everything_is_valid_local() {
        UUID uuid = UUIDFactory.Factories.UPROTOCOL.factory().create();
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest()
                .withId(uuid.toString())
                .withSource(URI.create("/body.access/1/door.front_left#Door"))
                .withType(UCloudEventType.PUBLISH.type());
        CloudEvent cloudEvent = builder.build();
        final CloudEventValidator validator = CloudEventValidator.Validators.PUBLISH.validator();
        final Status status = validator.validate(cloudEvent);
        assertEquals(ValidationResult.STATUS_SUCCESS, status);
    }

    @Test
    @DisplayName("Test microRemote Publish type CloudEvent is valid everything is valid")
    void test_publish_type_cloudevent_is_valid_when_everything_is_valid_remote() {
        UUID uuid = UUIDFactory.Factories.UPROTOCOL.factory().create();
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest()
                .withId(uuid.toString())
                .withSource(URI.create("//VCU.myvin/body.access/1/door.front_left#Door"))
                .withType(UCloudEventType.PUBLISH.type());
        CloudEvent cloudEvent = builder.build();
        final CloudEventValidator validator = CloudEventValidator.Validators.PUBLISH.validator();
        final Status status = validator.validate(cloudEvent);
        assertEquals(ValidationResult.STATUS_SUCCESS, status);
    }

    @Test
    @DisplayName("Test microRemote Publish type CloudEvent is valid everything is valid with a sink")
    void test_publish_type_cloudevent_is_valid_when_everything_is_valid_remote_with_a_sink() {
        UUID uuid = UUIDFactory.Factories.UPROTOCOL.factory().create();
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest()
                .withId(uuid.toString())
                .withSource(URI.create("//VCU.myvin/body.access/1/door.front_left#Door"))
                .withExtension("sink", "//bo.cloud/petapp")
                .withType(UCloudEventType.PUBLISH.type());
        CloudEvent cloudEvent = builder.build();
        final CloudEventValidator validator = CloudEventValidator.Validators.PUBLISH.validator();
        final Status status = validator.validate(cloudEvent);
        assertEquals(ValidationResult.STATUS_SUCCESS, status);
    }

    @Test
    @DisplayName("Test microRemote Publish type CloudEvent is not valid everything is valid with invalid sink")
    void test_publish_type_cloudevent_is_not_valid_when_remote_with_invalid_sink() {
        UUID uuid = UUIDFactory.Factories.UPROTOCOL.factory().create();
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest()
                .withId(uuid.toString())
                .withSource(URI.create("//VCU.myvin/body.access/1/door.front_left#Door"))
                .withExtension("sink", "//bo.cloud")
                .withType(UCloudEventType.PUBLISH.type());
        CloudEvent cloudEvent = builder.build();
        final CloudEventValidator validator = CloudEventValidator.Validators.PUBLISH.validator();
        final Status status = validator.validate(cloudEvent);
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status.getCode());
        assertEquals("Invalid CloudEvent sink [//bo.cloud]. UriPart is missing uSoftware Entity name.", status.getMessage());
    }

    @Test
    @DisplayName("Test Publish type CloudEvent is not valid when source is empty")
    void test_publish_type_cloudevent_is_not_valid_when_source_is_empty() {
        UUID uuid = UUIDFactory.Factories.UPROTOCOL.factory().create();
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest()
                .withId(uuid.toString())
                .withSource(URI.create("/"))
                .withType(UCloudEventType.PUBLISH.type());
        CloudEvent cloudEvent = builder.build();
        final CloudEventValidator validator = CloudEventValidator.Validators.PUBLISH.validator();
        final Status status = validator.validate(cloudEvent);
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status.getCode());
        assertEquals("Invalid Publish type CloudEvent source [/]. UriPart is missing uSoftware Entity name.", status.getMessage());
    }

    @Test
    @DisplayName("Test Publish type CloudEvent is not valid when source is invalid and id invalid")
    void test_publish_type_cloudevent_is_not_valid_when_source_is_missing_authority() {
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest()
                .withId("testme")
                .withSource(URI.create("/body.access"))
                .withType(UCloudEventType.PUBLISH.type());
        CloudEvent cloudEvent = builder.build();
        final CloudEventValidator validator = CloudEventValidator.Validators.PUBLISH.validator();
        final Status status = validator.validate(cloudEvent);
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status.getCode());
        assertEquals("Invalid CloudEvent Id [testme]. CloudEvent Id must be of type UUIDv8.," +
                "Invalid Publish type CloudEvent source [/body.access]. UriPart is missing uResource name.", status.getMessage());
    }

    @Test
    @DisplayName("Test Notification type CloudEvent is valid everything is valid")
    void test_notification_type_cloudevent_is_valid_when_everything_is_valid() {
        UUID uuid = UUIDFactory.Factories.UPROTOCOL.factory().create();
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest()
                .withId(uuid.toString())
                .withSource(URI.create("/body.access/1/door.front_left#Door"))
                .withType(UCloudEventType.PUBLISH.type())
                .withExtension("sink", "//bo.cloud/petapp");
        CloudEvent cloudEvent = builder.build();
        final CloudEventValidator validator = CloudEventValidator.Validators.NOTIFICATION.validator();
        final Status status = validator.validate(cloudEvent);
        assertEquals(ValidationResult.STATUS_SUCCESS, status);
    }

    @Test
    @DisplayName("Test Notification type CloudEvent is not valid missing sink")
    void test_notification_type_cloudevent_is_not_valid_missing_sink() {
        UUID uuid = UUIDFactory.Factories.UPROTOCOL.factory().create();
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest()
                .withId(uuid.toString())
                .withSource(URI.create("/body.access/1/door.front_left#Door"))
                .withType(UCloudEventType.PUBLISH.type());
        CloudEvent cloudEvent = builder.build();
        final CloudEventValidator validator = CloudEventValidator.Validators.NOTIFICATION.validator();
        final Status status = validator.validate(cloudEvent);
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status.getCode());
        assertEquals("Invalid CloudEvent sink. Notification CloudEvent sink must be an  uri.", status.getMessage());
    }

    @Test
    @DisplayName("Test Notification type CloudEvent is not valid invalid sink")
    void test_notification_type_cloudevent_is_not_valid_invalid_sink() {
        UUID uuid = UUIDFactory.Factories.UPROTOCOL.factory().create();
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest()
                .withId(uuid.toString())
                .withSource(URI.create("/body.access/1/door.front_left#Door"))
                .withType(UCloudEventType.PUBLISH.type())
                .withExtension("sink", "//bo.cloud");
        CloudEvent cloudEvent = builder.build();
        final CloudEventValidator validator = CloudEventValidator.Validators.NOTIFICATION.validator();
        final Status status = validator.validate(cloudEvent);
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status.getCode());
        assertEquals("Invalid Notification type CloudEvent sink [//bo.cloud]. UriPart is missing uSoftware Entity name.", status.getMessage());
    }


    @Test
    @DisplayName("Test Request type CloudEvent is valid everything is valid")
    void test_request_type_cloudevent_is_valid_when_everything_is_valid() {
        UUID uuid = UUIDFactory.Factories.UPROTOCOL.factory().create();
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest()
                .withId(uuid.toString())
                .withSource(URI.create("//bo.cloud/petapp//rpc.response"))
                .withType(UCloudEventType.REQUEST.type())
                .withExtension("sink", "//VCU.myvin/body.access/1/rpc.UpdateDoor");
        CloudEvent cloudEvent = builder.build();
        final CloudEventValidator validator = CloudEventValidator.Validators.REQUEST.validator();
        final Status status = validator.validate(cloudEvent);
        assertEquals(ValidationResult.STATUS_SUCCESS, status);
    }

    @Test
    @DisplayName("Test Request type CloudEvent is not valid invalid source")
    void test_request_type_cloudevent_is_not_valid_invalid_source() {
        UUID uuid = UUIDFactory.Factories.UPROTOCOL.factory().create();
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest()
                .withId(uuid.toString())
                .withSource(URI.create("//bo.cloud/petapp//dog"))
                .withExtension("sink", "//VCU.myvin/body.access/1/rpc.UpdateDoor")
                .withType(UCloudEventType.REQUEST.type());
        CloudEvent cloudEvent = builder.build();
        final CloudEventValidator validator = CloudEventValidator.Validators.REQUEST.validator();
        final Status status = validator.validate(cloudEvent);
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status.getCode());
        assertEquals("Invalid RPC Request CloudEvent source [//bo.cloud/petapp//dog]. " +
                "Invalid RPC uri application response topic. UriPart is missing rpc.response.", status.getMessage());
    }

    @Test
    @DisplayName("Test Request type CloudEvent is not valid missing sink")
    void test_request_type_cloudevent_is_not_valid_missing_sink() {
        UUID uuid = UUIDFactory.Factories.UPROTOCOL.factory().create();
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest()
                .withId(uuid.toString())
                .withSource(URI.create("//bo.cloud/petapp//rpc.response"))
                .withType(UCloudEventType.REQUEST.type());
        CloudEvent cloudEvent = builder.build();
        final CloudEventValidator validator = CloudEventValidator.Validators.REQUEST.validator();
        final Status status = validator.validate(cloudEvent);
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status.getCode());
        assertEquals("Invalid RPC Request CloudEvent sink. Request CloudEvent sink must be uri for the method to be called.", status.getMessage());
    }

    @Test
    @DisplayName("Test Request type CloudEvent is not valid sink not rpc command")
    void test_request_type_cloudevent_is_not_valid_invalid_sink_not_rpc_command() {
        UUID uuid = UUIDFactory.Factories.UPROTOCOL.factory().create();
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest()
                .withId(uuid.toString())
                .withSource(URI.create("//bo.cloud/petapp//rpc.response"))
                .withType(UCloudEventType.REQUEST.type())
                .withExtension("sink", "//VCU.myvin/body.access/1/UpdateDoor");
        CloudEvent cloudEvent = builder.build();
        final CloudEventValidator validator = CloudEventValidator.Validators.REQUEST.validator();
        final Status status = validator.validate(cloudEvent);
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status.getCode());
        assertEquals("Invalid RPC Request CloudEvent sink [//VCU.myvin/body.access/1/UpdateDoor]. " +
                "Invalid RPC method uri. UriPart should be the method to be called, or method from response.", status.getMessage());
    }

    @Test
    @DisplayName("Test Response type CloudEvent is valid everything is valid")
    void test_response_type_cloudevent_is_valid_when_everything_is_valid() {
        UUID uuid = UUIDFactory.Factories.UPROTOCOL.factory().create();
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest()
                .withId(uuid.toString())
                .withSource(URI.create("//VCU.myvin/body.access/1/rpc.UpdateDoor"))
                .withType(UCloudEventType.RESPONSE.type())
                .withExtension("sink", "//bo.cloud/petapp//rpc.response");
        CloudEvent cloudEvent = builder.build();
        final CloudEventValidator validator = CloudEventValidator.Validators.RESPONSE.validator();
        final Status status = validator.validate(cloudEvent);
        assertEquals(ValidationResult.STATUS_SUCCESS, status);
    }

    @Test
    @DisplayName("Test Response type CloudEvent is not valid invalid source")
    void test_response_type_cloudevent_is_not_valid_invalid_source() {
        UUID uuid = UUIDFactory.Factories.UPROTOCOL.factory().create();
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest()
                .withId(uuid.toString())
                .withSource(URI.create("//VCU.myvin/body.access/1/UpdateDoor"))
                .withExtension("sink", "//bo.cloud/petapp//rpc.response")
                .withType(UCloudEventType.RESPONSE.type());
        CloudEvent cloudEvent = builder.build();
        final CloudEventValidator validator = CloudEventValidator.Validators.RESPONSE.validator();
        final Status status = validator.validate(cloudEvent);
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status.getCode());
        assertEquals("Invalid RPC Response CloudEvent source [//VCU.myvin/body.access/1/UpdateDoor]. " +
                "Invalid RPC method uri. UriPart should be the method to be called, or method from response.", status.getMessage());
    }

    @Test
    @DisplayName("Test Response type CloudEvent is not valid missing sink and invalid source")
    void test_response_type_cloudevent_is_not_valid_missing_sink_and_invalid_source() {
        UUID uuid = UUIDFactory.Factories.UPROTOCOL.factory().create();
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest()
                .withId(uuid.toString())
                .withSource(URI.create("//VCU.myvin/body.access/1/UpdateDoor"))
                .withType(UCloudEventType.RESPONSE.type());
        CloudEvent cloudEvent = builder.build();
        final CloudEventValidator validator = CloudEventValidator.Validators.RESPONSE.validator();
        final Status status = validator.validate(cloudEvent);
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status.getCode());
        assertEquals("Invalid RPC Response CloudEvent source [//VCU.myvin/body.access/1/UpdateDoor]. " +
                "Invalid RPC method uri. UriPart should be the method to be called, or method from response.," +
                "Invalid CloudEvent sink. Response CloudEvent sink must be uri the destination of the response.", status.getMessage());
    }

    @Test
    @DisplayName("Test Response type CloudEvent is not valid source not rpc command")
    void test_response_type_cloudevent_is_not_valid_invalid_source_not_rpc_command() {
        UUID uuid = UUIDFactory.Factories.UPROTOCOL.factory().create();
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest()
                .withId(uuid.toString())
                .withSource(URI.create("//bo.cloud/petapp/1/dog"))
                .withType(UCloudEventType.RESPONSE.type())
                .withExtension("sink", "//VCU.myvin/body.access/1/UpdateDoor");
        CloudEvent cloudEvent = builder.build();
        final CloudEventValidator validator = CloudEventValidator.Validators.RESPONSE.validator();
        final Status status = validator.validate(cloudEvent);
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status.getCode());
        assertEquals("Invalid RPC Response CloudEvent source [//bo.cloud/petapp/1/dog]. Invalid RPC method uri. UriPart should be the method to be called, or method from response.," +
                "Invalid RPC Response CloudEvent sink [//VCU.myvin/body.access/1/UpdateDoor]. " +
                "Invalid RPC uri application response topic. UriPart is missing rpc.response.", status.getMessage());
    }

    private CloudEventBuilder buildBaseCloudEventBuilderForTest() {
        // source
        UEntity use = UEntity.longFormat("body.access");
        UUri Uri = new UUri(UAuthority.local(), use,
                UResource.longFormat("door", "front_left", "Door"));
        String source = UriSerializer.LONG.serialize(Uri);

        // fake payload
        final Any protoPayload = buildProtoPayloadForTest();

        // additional attributes
        final UCloudEventAttributes uCloudEventAttributes = new UCloudEventAttributes.UCloudEventAttributesBuilder()
                .withHash("somehash")
                .withPriority(UCloudEventAttributes.Priority.STANDARD)
                .withTtl(3)
                .withToken("someOAuthToken")
                .build();

        // build the cloud event
        final CloudEventBuilder cloudEventBuilder = CloudEventFactory.buildBaseCloudEvent("testme", source,
                protoPayload.toByteArray(), protoPayload.getTypeUrl(),
                uCloudEventAttributes);
        cloudEventBuilder.withType(UCloudEventType.PUBLISH.type());

        return cloudEventBuilder;
    }

    private Any buildProtoPayloadForTest() {
        io.cloudevents.v1.proto.CloudEvent cloudEventProto = io.cloudevents.v1.proto.CloudEvent.newBuilder()
                .setSpecVersion("1.0")
                .setId("hello")
                .setSource("/body.access")
                .setType("example.demo")
                .setProtoData(Any.newBuilder().build())
                .build();
        return Any.pack(cloudEventProto);
    }

}