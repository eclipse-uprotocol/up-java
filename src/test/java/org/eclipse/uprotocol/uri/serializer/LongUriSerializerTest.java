package org.eclipse.uprotocol.uri.serializer;

import org.eclipse.uprotocol.uri.datamodel.UUri;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.uprotocol.uri.datamodel.UAuthority;
import org.eclipse.uprotocol.uri.datamodel.UEntity;
import org.eclipse.uprotocol.uri.datamodel.UResource;

public class LongUriSerializerTest {

    @Test
    @DisplayName("Test using the serializers")
    public void test_using_the_serializers() {
        final UUri uri = new UUri(UAuthority.local(), UEntity.longFormat("hartley"), UResource.forRpcRequest("raise"));
        final String strUri = UriSerializer.LONG.serialize(uri);
        assertEquals("/hartley//rpc.raise", strUri);
        final UUri uri2 = UriSerializer.LONG.deserialize(strUri);
        assertTrue(uri.equals(uri2));
    }

        @Test
    @DisplayName("Test parse uProtocol uri that is null")
    public void test_parse_protocol_uri_when_is_null() {
        UUri Uri = UriSerializer.LONG.deserialize(null);
        assertTrue(Uri.isEmpty());
    }

    
    @Test
    @DisplayName("Test parse uProtocol uri that is empty string")
    public void test_parse_protocol_uri_when_is_empty_string() {
        String uri = "";
        UUri Uri = UriSerializer.LONG.deserialize(uri);
        assertTrue(Uri.isEmpty());

        String uri2 = UriSerializer.LONG.serialize(null);
        assertTrue(uri2.isEmpty());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with schema and slash")
    public void test_parse_protocol_uri_with_schema_and_slash() {
        String uri = "/";
        UUri Uri = UriSerializer.LONG.deserialize(uri);
        assertTrue(Uri.uAuthority().isLocal());
        assertFalse(Uri.uAuthority().isMarkedRemote());
        assertTrue(Uri.isEmpty());

        String uri2 = UriSerializer.LONG.serialize(UUri.empty());
        assertTrue(uri2.isEmpty());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with schema and double slash")
    public void test_parse_protocol_uri_with_schema_and_double_slash() {
        String uri = "//";
        UUri Uri = UriSerializer.LONG.deserialize(uri);
        assertTrue(Uri.uAuthority().isLocal());
        assertTrue(Uri.uAuthority().isMarkedRemote());
        assertTrue(Uri.isEmpty());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with schema and 3 slash and something")
    public void test_parse_protocol_uri_with_schema_and_3_slash_and_something() {
        String uri = "///body.access";
        UUri Uri = UriSerializer.LONG.deserialize(uri);
        assertTrue(Uri.uAuthority().isLocal());
        assertTrue(Uri.uAuthority().isMarkedRemote());
        assertEquals("body.access", Uri.uEntity().name());
        assertTrue(Uri.uEntity().version().isEmpty());
        assertTrue(Uri.uResource().isEmpty());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with schema and 4 slash and something")
    public void test_parse_protocol_uri_with_schema_and_4_slash_and_something() {
        String uri = "////body.access";
        UUri Uri = UriSerializer.LONG.deserialize(uri);
        assertTrue(Uri.uAuthority().isLocal());
        assertTrue(Uri.uAuthority().isMarkedRemote());
        assertTrue(Uri.uEntity().name().isBlank());
        assertTrue(Uri.uEntity().version().isEmpty());
        assertTrue(Uri.uResource().isEmpty());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with schema and 5 slash and something")
    public void test_parse_protocol_uri_with_schema_and_5_slash_and_something() {
        String uri = "/////body.access";
        UUri Uri = UriSerializer.LONG.deserialize(uri);
        assertTrue(Uri.uAuthority().isLocal());
        assertTrue(Uri.uAuthority().isMarkedRemote());
        assertTrue(Uri.uEntity().isEmpty());
        assertEquals("body", Uri.uResource().name());
        assertTrue(Uri.uResource().instance().isPresent());
        assertEquals("access", Uri.uResource().instance().get());
        assertTrue(Uri.uResource().message().isEmpty());
    }
 
    @Test
    @DisplayName("Test parse uProtocol uri with schema and 6 slash and something")
    public void test_parse_protocol_uri_with_schema_and_6_slash_and_something() {
        String uri = "//////body.access";
        UUri Uri = UriSerializer.LONG.deserialize(uri);
        assertTrue(Uri.uAuthority().isLocal());
        assertTrue(Uri.uAuthority().isMarkedRemote());
        assertTrue(Uri.isEmpty());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with local service no version")
    public void test_parse_protocol_uri_with_local_service_no_version() {
        String uri = "/body.access";
        UUri Uri = UriSerializer.LONG.deserialize(uri);
        assertTrue(Uri.uAuthority().isLocal());
        assertFalse(Uri.uAuthority().isMarkedRemote());
        assertEquals("body.access", Uri.uEntity().name());
        assertTrue(Uri.uEntity().version().isEmpty());
        assertTrue(Uri.uResource().isEmpty());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with local service with version")
    public void test_parse_protocol_uri_with_local_service_with_version() {
        String uri = "/body.access/1";
        UUri Uri = UriSerializer.LONG.deserialize(uri);
        assertTrue(Uri.uAuthority().isLocal());
        assertFalse(Uri.uAuthority().isMarkedRemote());
        assertEquals("body.access", Uri.uEntity().name());
        assertTrue(Uri.uEntity().version().isPresent());
        assertEquals(1, Uri.uEntity().version().get());
        assertTrue(Uri.uResource().isEmpty());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with local service no version with resource name only")
    public void test_parse_protocol_uri_with_local_service_no_version_with_resource_name_only() {
        String uri = "/body.access//door";
        UUri Uri = UriSerializer.LONG.deserialize(uri);
        assertTrue(Uri.uAuthority().isLocal());
        assertFalse(Uri.uAuthority().isMarkedRemote());
        assertEquals("body.access", Uri.uEntity().name());
        assertTrue(Uri.uEntity().version().isEmpty());
        assertEquals("door", Uri.uResource().name());
        assertTrue(Uri.uResource().instance().isEmpty());
        assertTrue(Uri.uResource().message().isEmpty());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with local service with version with resource name only")
    public void test_parse_protocol_uri_with_local_service_with_version_with_resource_name_only() {
        String uri = "/body.access/1/door";
        UUri Uri = UriSerializer.LONG.deserialize(uri);
        assertTrue(Uri.uAuthority().isLocal());
        assertFalse(Uri.uAuthority().isMarkedRemote());
        assertEquals("body.access", Uri.uEntity().name());
        assertTrue(Uri.uEntity().version().isPresent());
        assertEquals(1, Uri.uEntity().version().get());
        assertEquals("door", Uri.uResource().name());
        assertTrue(Uri.uResource().instance().isEmpty());
        assertTrue(Uri.uResource().message().isEmpty());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with local service no version with resource and instance only")
    public void test_parse_protocol_uri_with_local_service_no_version_with_resource_with_instance() {
        String uri = "/body.access//door.front_left";
        UUri Uri = UriSerializer.LONG.deserialize(uri);
        assertTrue(Uri.uAuthority().isLocal());
        assertFalse(Uri.uAuthority().isMarkedRemote());
        assertEquals("body.access", Uri.uEntity().name());
        assertTrue(Uri.uEntity().version().isEmpty());
        assertEquals("door", Uri.uResource().name());
        assertTrue(Uri.uResource().instance().isPresent());
        assertEquals("front_left", Uri.uResource().instance().get());
        assertTrue(Uri.uResource().message().isEmpty());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with local service with version with resource and instance only")
    public void test_parse_protocol_uri_with_local_service_with_version_with_resource_with_message() {
        String uri = "/body.access/1/door.front_left";
        UUri Uri = UriSerializer.LONG.deserialize(uri);
        assertTrue(Uri.uAuthority().isLocal());
        assertFalse(Uri.uAuthority().isMarkedRemote());
        assertEquals("body.access", Uri.uEntity().name());
        assertTrue(Uri.uEntity().version().isPresent());
        assertEquals(1, Uri.uEntity().version().get());
        assertEquals("door", Uri.uResource().name());
        assertTrue(Uri.uResource().instance().isPresent());
        assertEquals("front_left", Uri.uResource().instance().get());
        assertTrue(Uri.uResource().message().isEmpty());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with local service no version with resource with instance and message")
    public void test_parse_protocol_uri_with_local_service_no_version_with_resource_with_instance_and_message() {
        String uri = "/body.access//door.front_left#Door";
        UUri Uri = UriSerializer.LONG.deserialize(uri);
        assertTrue(Uri.uAuthority().isLocal());
        assertFalse(Uri.uAuthority().isMarkedRemote());
        assertEquals("body.access", Uri.uEntity().name());
        assertTrue(Uri.uEntity().version().isEmpty());
        assertEquals("door", Uri.uResource().name());
        assertTrue(Uri.uResource().instance().isPresent());
        assertEquals("front_left", Uri.uResource().instance().get());
        assertTrue(Uri.uResource().message().isPresent());
        assertEquals("Door", Uri.uResource().message().get());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with local service with version with resource with instance and message")
    public void test_parse_protocol_uri_with_local_service_with_version_with_resource_with_instance_and_message() {
        String uri = "/body.access/1/door.front_left#Door";
        UUri Uri = UriSerializer.LONG.deserialize(uri);
        assertTrue(Uri.uAuthority().isLocal());
        assertFalse(Uri.uAuthority().isMarkedRemote());
        assertEquals("body.access", Uri.uEntity().name());
        assertTrue(Uri.uEntity().version().isPresent());
        assertEquals(1, Uri.uEntity().version().get());
        assertEquals("door", Uri.uResource().name());
        assertTrue(Uri.uResource().instance().isPresent());
        assertEquals("front_left", Uri.uResource().instance().get());
        assertTrue(Uri.uResource().message().isPresent());
        assertEquals("Door", Uri.uResource().message().get());
    }

    @Test
    @DisplayName("Test parse uProtocol RPC uri with local service no version")
    public void test_parse_protocol_rpc_uri_with_local_service_no_version() {
        String uri = "/petapp//rpc.response";
        UUri Uri = UriSerializer.LONG.deserialize(uri);
        assertTrue(Uri.uAuthority().isLocal());
        assertFalse(Uri.uAuthority().isMarkedRemote());
        assertEquals("petapp", Uri.uEntity().name());
        assertTrue(Uri.uEntity().version().isEmpty());
        assertEquals("rpc", Uri.uResource().name());
        assertTrue(Uri.uResource().instance().isPresent());
        assertEquals("response", Uri.uResource().instance().get());
        assertTrue(Uri.uResource().message().isEmpty());
    }

    @Test
    @DisplayName("Test parse uProtocol RPC uri with local service with version")
    public void test_parse_protocol_rpc_uri_with_local_service_with_version() {
        String uri = "/petapp/1/rpc.response";
        UUri Uri = UriSerializer.LONG.deserialize(uri);
        assertTrue(Uri.uAuthority().isLocal());
        assertFalse(Uri.uAuthority().isMarkedRemote());
        assertEquals("petapp", Uri.uEntity().name());
        assertTrue(Uri.uEntity().version().isPresent());
        assertEquals(1, Uri.uEntity().version().get());
        assertEquals("rpc", Uri.uResource().name());
        assertTrue(Uri.uResource().instance().isPresent());
        assertEquals("response", Uri.uResource().instance().get());
        assertTrue(Uri.uResource().message().isEmpty());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with microRemote service only device no domain")
    public void test_parse_protocol_uri_with_remote_service_only_device_no_domain() {
        String uri = "//VCU";
        UUri Uri = UriSerializer.LONG.deserialize(uri);
        assertTrue(Uri.uAuthority().isRemote());
        assertTrue(Uri.uAuthority().device().isPresent());
        assertEquals("vcu", Uri.uAuthority().device().get());
        assertTrue(Uri.uAuthority().domain().isEmpty());
        assertTrue(Uri.uEntity().isEmpty());
        assertTrue(Uri.uResource().isEmpty());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with microRemote service only device and domain")
    public void test_parse_protocol_uri_with_remote_service_only_device_and_domain() {
        String uri = "//VCU.MY_CAR_VIN";
        UUri Uri = UriSerializer.LONG.deserialize(uri);
        assertTrue(Uri.uAuthority().isRemote());
        assertTrue(Uri.uAuthority().device().isPresent());
        assertEquals("vcu", Uri.uAuthority().device().get());
        assertTrue(Uri.uAuthority().domain().isPresent());
        assertEquals("my_car_vin", Uri.uAuthority().domain().get());
        assertTrue(Uri.uEntity().isEmpty());
        assertTrue(Uri.uResource().isEmpty());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with microRemote service only device and cloud domain")
    public void test_parse_protocol_uri_with_remote_service_only_device_and_cloud_domain() {
        String uri = "//cloud.uprotocol.example.com";
        UUri Uri = UriSerializer.LONG.deserialize(uri);
        assertTrue(Uri.uAuthority().isRemote());
        assertTrue(Uri.uAuthority().device().isPresent());
        assertEquals("cloud", Uri.uAuthority().device().get());
        assertTrue(Uri.uAuthority().domain().isPresent());
        assertEquals("uprotocol.example.com", Uri.uAuthority().domain().get());
        assertTrue(Uri.uEntity().isEmpty());
        assertTrue(Uri.uResource().isEmpty());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with microRemote service no version")
    public void test_parse_protocol_uri_with_remote_service_no_version() {
        String uri = "//VCU.MY_CAR_VIN/body.access";
        UUri Uri = UriSerializer.LONG.deserialize(uri);
        assertTrue(Uri.uAuthority().isRemote());
        assertTrue(Uri.uAuthority().device().isPresent());
        assertEquals("vcu", Uri.uAuthority().device().get());
        assertTrue(Uri.uAuthority().domain().isPresent());
        assertEquals("my_car_vin", Uri.uAuthority().domain().get());
        assertEquals("body.access", Uri.uEntity().name());
        assertTrue(Uri.uEntity().version().isEmpty());
        assertTrue(Uri.uResource().isEmpty());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with microRemote cloud service no version")
    public void test_parse_protocol_uri_with_remote_cloud_service_no_version() {
        String uri = "//cloud.uprotocol.example.com/body.access";
        UUri Uri = UriSerializer.LONG.deserialize(uri);
        assertTrue(Uri.uAuthority().isRemote());
        assertTrue(Uri.uAuthority().device().isPresent());
        assertEquals("cloud", Uri.uAuthority().device().get());
        assertTrue(Uri.uAuthority().domain().isPresent());
        assertEquals("uprotocol.example.com", Uri.uAuthority().domain().get());
        assertEquals("body.access", Uri.uEntity().name());
        assertTrue(Uri.uEntity().version().isEmpty());
        assertTrue(Uri.uResource().isEmpty());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with microRemote service with version")
    public void test_parse_protocol_uri_with_remote_service_with_version() {
        String uri = "//VCU.MY_CAR_VIN/body.access/1";
        UUri Uri = UriSerializer.LONG.deserialize(uri);
        assertTrue(Uri.uAuthority().isRemote());
        assertTrue(Uri.uAuthority().device().isPresent());
        assertEquals("vcu", Uri.uAuthority().device().get());
        assertTrue(Uri.uAuthority().domain().isPresent());
        assertEquals("my_car_vin", Uri.uAuthority().domain().get());
        assertEquals("body.access", Uri.uEntity().name());
        assertTrue(Uri.uEntity().version().isPresent());
        assertEquals(1, Uri.uEntity().version().get());
        assertTrue(Uri.uResource().isEmpty());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with microRemote cloud service with version")
    public void test_parse_protocol_uri_with_remote_cloud_service_with_version() {
        String uri = "//cloud.uprotocol.example.com/body.access/1";
        UUri Uri = UriSerializer.LONG.deserialize(uri);
        assertTrue(Uri.uAuthority().isRemote());
        assertTrue(Uri.uAuthority().device().isPresent());
        assertEquals("cloud", Uri.uAuthority().device().get());
        assertTrue(Uri.uAuthority().domain().isPresent());
        assertEquals("uprotocol.example.com", Uri.uAuthority().domain().get());
        assertEquals("body.access", Uri.uEntity().name());
        assertTrue(Uri.uEntity().version().isPresent());
        assertEquals(1, Uri.uEntity().version().get());
        assertTrue(Uri.uResource().isEmpty());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with microRemote service no version with resource name only")
    public void test_parse_protocol_uri_with_remote_service_no_version_with_resource_name_only() {
        String uri = "//VCU.MY_CAR_VIN/body.access//door";
        UUri Uri = UriSerializer.LONG.deserialize(uri);
        assertTrue(Uri.uAuthority().isRemote());
        assertTrue(Uri.uAuthority().device().isPresent());
        assertEquals("vcu", Uri.uAuthority().device().get());
        assertTrue(Uri.uAuthority().domain().isPresent());
        assertEquals("my_car_vin", Uri.uAuthority().domain().get());
        assertEquals("body.access", Uri.uEntity().name());
        assertTrue(Uri.uEntity().version().isEmpty());
        assertEquals("door", Uri.uResource().name());
        assertTrue(Uri.uResource().instance().isEmpty());
        assertTrue(Uri.uResource().message().isEmpty());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with microRemote cloud service no version with resource name only")
    public void test_parse_protocol_uri_with_remote_cloud_service_no_version_with_resource_name_only() {
        String uri = "//cloud.uprotocol.example.com/body.access//door";
        UUri Uri = UriSerializer.LONG.deserialize(uri);
        assertTrue(Uri.uAuthority().isRemote());
        assertTrue(Uri.uAuthority().device().isPresent());
        assertEquals("cloud", Uri.uAuthority().device().get());
        assertTrue(Uri.uAuthority().domain().isPresent());
        assertEquals("uprotocol.example.com", Uri.uAuthority().domain().get());
        assertEquals("body.access", Uri.uEntity().name());
        assertTrue(Uri.uEntity().version().isEmpty());
        assertEquals("door", Uri.uResource().name());
        assertTrue(Uri.uResource().instance().isEmpty());
        assertTrue(Uri.uResource().message().isEmpty());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with microRemote service with version with resource name only")
    public void test_parse_protocol_uri_with_remote_service_with_version_with_resource_name_only() {
        String uri = "//VCU.MY_CAR_VIN/body.access/1/door";
        UUri Uri = UriSerializer.LONG.deserialize(uri);
        assertTrue(Uri.uAuthority().isRemote());
        assertTrue(Uri.uAuthority().device().isPresent());
        assertEquals("vcu", Uri.uAuthority().device().get());
        assertTrue(Uri.uAuthority().domain().isPresent());
        assertEquals("my_car_vin", Uri.uAuthority().domain().get());
        assertEquals("body.access", Uri.uEntity().name());
        assertTrue(Uri.uEntity().version().isPresent());
        assertEquals(1, Uri.uEntity().version().get());
        assertEquals("door", Uri.uResource().name());
        assertTrue(Uri.uResource().instance().isEmpty());
        assertTrue(Uri.uResource().message().isEmpty());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with microRemote cloud service with version with resource name only")
    public void test_parse_protocol_uri_with_remote_service_cloud_with_version_with_resource_name_only() {
        String uri = "//cloud.uprotocol.example.com/body.access/1/door";
        UUri Uri = UriSerializer.LONG.deserialize(uri);
        assertTrue(Uri.uAuthority().isRemote());
        assertTrue(Uri.uAuthority().device().isPresent());
        assertEquals("cloud", Uri.uAuthority().device().get());
        assertTrue(Uri.uAuthority().domain().isPresent());
        assertEquals("uprotocol.example.com", Uri.uAuthority().domain().get());
        assertEquals("body.access", Uri.uEntity().name());
        assertTrue(Uri.uEntity().version().isPresent());
        assertEquals(1, Uri.uEntity().version().get());
        assertEquals("door", Uri.uResource().name());
        assertTrue(Uri.uResource().instance().isEmpty());
        assertTrue(Uri.uResource().message().isEmpty());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with microRemote service no version with resource and instance no message")
    public void test_parse_protocol_uri_with_remote_service_no_version_with_resource_and_instance_no_message() {
        String uri = "//VCU.MY_CAR_VIN/body.access//door.front_left";
        UUri Uri = UriSerializer.LONG.deserialize(uri);
        assertTrue(Uri.uAuthority().isRemote());
        assertTrue(Uri.uAuthority().device().isPresent());
        assertEquals("vcu", Uri.uAuthority().device().get());
        assertTrue(Uri.uAuthority().domain().isPresent());
        assertEquals("my_car_vin", Uri.uAuthority().domain().get());
        assertEquals("body.access", Uri.uEntity().name());
        assertTrue(Uri.uEntity().version().isEmpty());
        assertEquals("door", Uri.uResource().name());
        assertTrue(Uri.uResource().instance().isPresent());
        assertEquals("front_left", Uri.uResource().instance().get());
        assertTrue(Uri.uResource().message().isEmpty());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with microRemote service with version with resource and instance no message")
    public void test_parse_protocol_uri_with_remote_service_with_version_with_resource_and_instance_no_message() {
        String uri = "//VCU.MY_CAR_VIN/body.access/1/door.front_left";
        UUri Uri = UriSerializer.LONG.deserialize(uri);
        assertTrue(Uri.uAuthority().isRemote());
        assertTrue(Uri.uAuthority().device().isPresent());
        assertEquals("vcu", Uri.uAuthority().device().get());
        assertTrue(Uri.uAuthority().domain().isPresent());
        assertEquals("my_car_vin", Uri.uAuthority().domain().get());
        assertEquals("body.access", Uri.uEntity().name());
        assertTrue(Uri.uEntity().version().isPresent());
        assertEquals(1, Uri.uEntity().version().get());
        assertEquals("door", Uri.uResource().name());
        assertTrue(Uri.uResource().instance().isPresent());
        assertEquals("front_left", Uri.uResource().instance().get());
        assertTrue(Uri.uResource().message().isEmpty());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with microRemote service no version with resource and instance and message")
    public void test_parse_protocol_uri_with_remote_service_no_version_with_resource_and_instance_and_message() {
        String uri = "//VCU.MY_CAR_VIN/body.access//door.front_left#Door";
        UUri Uri = UriSerializer.LONG.deserialize(uri);
        assertTrue(Uri.uAuthority().isRemote());
        assertTrue(Uri.uAuthority().device().isPresent());
        assertEquals("vcu", Uri.uAuthority().device().get());
        assertTrue(Uri.uAuthority().domain().isPresent());
        assertEquals("my_car_vin", Uri.uAuthority().domain().get());
        assertEquals("body.access", Uri.uEntity().name());
        assertTrue(Uri.uEntity().version().isEmpty());
        assertEquals("door", Uri.uResource().name());
        assertTrue(Uri.uResource().instance().isPresent());
        assertEquals("front_left", Uri.uResource().instance().get());
        assertTrue(Uri.uResource().message().isPresent());
        assertEquals("Door", Uri.uResource().message().get());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with microRemote cloud service no version with resource and instance and message")
    public void test_parse_protocol_uri_with_remote_cloud_service_no_version_with_resource_and_instance_and_message() {
        String uri = "//cloud.uprotocol.example.com/body.access//door.front_left#Door";
        UUri Uri = UriSerializer.LONG.deserialize(uri);
        assertTrue(Uri.uAuthority().isRemote());
        assertTrue(Uri.uAuthority().device().isPresent());
        assertEquals("cloud", Uri.uAuthority().device().get());
        assertTrue(Uri.uAuthority().domain().isPresent());
        assertEquals("uprotocol.example.com", Uri.uAuthority().domain().get());
        assertEquals("body.access", Uri.uEntity().name());
        assertTrue(Uri.uEntity().version().isEmpty());
        assertEquals("door", Uri.uResource().name());
        assertTrue(Uri.uResource().instance().isPresent());
        assertEquals("front_left", Uri.uResource().instance().get());
        assertTrue(Uri.uResource().message().isPresent());
        assertEquals("Door", Uri.uResource().message().get());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with microRemote service with version with resource and instance and message")
    public void test_parse_protocol_uri_with_remote_service_with_version_with_resource_and_instance_and_message() {
        String uri = "//VCU.MY_CAR_VIN/body.access/1/door.front_left#Door";
        UUri Uri = UriSerializer.LONG.deserialize(uri);
        assertTrue(Uri.uAuthority().isRemote());
        assertTrue(Uri.uAuthority().device().isPresent());
        assertEquals("vcu", Uri.uAuthority().device().get());
        assertTrue(Uri.uAuthority().domain().isPresent());
        assertEquals("my_car_vin", Uri.uAuthority().domain().get());
        assertEquals("body.access", Uri.uEntity().name());
        assertTrue(Uri.uEntity().version().isPresent());
        assertEquals(1, Uri.uEntity().version().get());
        assertEquals("door", Uri.uResource().name());
        assertTrue(Uri.uResource().instance().isPresent());
        assertEquals("front_left", Uri.uResource().instance().get());
        assertTrue(Uri.uResource().message().isPresent());
        assertEquals("Door", Uri.uResource().message().get());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with microRemote cloud service with version with resource and instance and message")
    public void test_parse_protocol_uri_with_remote_cloud_service_with_version_with_resource_and_instance_and_message() {
        String uri = "//cloud.uprotocol.example.com/body.access/1/door.front_left#Door";
        UUri Uri = UriSerializer.LONG.deserialize(uri);
        assertTrue(Uri.uAuthority().isRemote());
        assertTrue(Uri.uAuthority().device().isPresent());
        assertEquals("cloud", Uri.uAuthority().device().get());
        assertTrue(Uri.uAuthority().domain().isPresent());
        assertEquals("uprotocol.example.com", Uri.uAuthority().domain().get());
        assertEquals("body.access", Uri.uEntity().name());
        assertTrue(Uri.uEntity().version().isPresent());
        assertEquals(1, Uri.uEntity().version().get());
        assertEquals("door", Uri.uResource().name());
        assertTrue(Uri.uResource().instance().isPresent());
        assertEquals("front_left", Uri.uResource().instance().get());
        assertTrue(Uri.uResource().message().isPresent());
        assertEquals("Door", Uri.uResource().message().get());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with microRemote service with version with resource with message when there is only device, no domain")
    public void test_parse_protocol_uri_with_remote_service_with_version_with_resource_with_message_device_no_domain() {
        String uri = "//VCU/body.access/1/door.front_left";
        UUri Uri = UriSerializer.LONG.deserialize(uri);
        assertTrue(Uri.uAuthority().isRemote());
        assertTrue(Uri.uAuthority().device().isPresent());
        assertEquals("vcu", Uri.uAuthority().device().get());
        assertTrue(Uri.uAuthority().domain().isEmpty());
        assertEquals("body.access", Uri.uEntity().name());
        assertTrue(Uri.uEntity().version().isPresent());
        assertEquals(1, Uri.uEntity().version().get());
        assertEquals("door", Uri.uResource().name());
        assertTrue(Uri.uResource().instance().isPresent());
        assertEquals("front_left", Uri.uResource().instance().get());
        assertTrue(Uri.uResource().message().isEmpty());
    }

    @Test
    @DisplayName("Test parse uProtocol RPC uri with microRemote service no version")
    public void test_parse_protocol_rpc_uri_with_remote_service_no_version() {
        String uri = "//bo.cloud/petapp//rpc.response";
        UUri Uri = UriSerializer.LONG.deserialize(uri);
        assertTrue(Uri.uAuthority().isRemote());
        assertTrue(Uri.uAuthority().device().isPresent());
        assertEquals("bo", Uri.uAuthority().device().get());
        assertTrue(Uri.uAuthority().domain().isPresent());
        assertEquals("cloud", Uri.uAuthority().domain().get());
        assertEquals("petapp", Uri.uEntity().name());
        assertTrue(Uri.uEntity().version().isEmpty());
        assertEquals("rpc", Uri.uResource().name());
        assertTrue(Uri.uResource().instance().isPresent());
        assertEquals("response", Uri.uResource().instance().get());
        assertTrue(Uri.uResource().message().isEmpty());
    }

    @Test
    @DisplayName("Test parse uProtocol RPC uri with microRemote service with version")
    public void test_parse_protocol_rpc_uri_with_remote_service_with_version() {
        String uri = "//bo.cloud/petapp/1/rpc.response";
        UUri Uri = UriSerializer.LONG.deserialize(uri);
        assertTrue(Uri.uAuthority().isRemote());
        assertTrue(Uri.uAuthority().device().isPresent());
        assertEquals("bo", Uri.uAuthority().device().get());
        assertTrue(Uri.uAuthority().domain().isPresent());
        assertEquals("cloud", Uri.uAuthority().domain().get());
        assertEquals("petapp", Uri.uEntity().name());
        assertTrue(Uri.uEntity().version().isPresent());
        assertEquals(1, Uri.uEntity().version().get());
        assertEquals("rpc", Uri.uResource().name());
        assertTrue(Uri.uResource().instance().isPresent());
        assertEquals("response", Uri.uResource().instance().get());
        assertTrue(Uri.uResource().message().isEmpty());
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from null")
    public void test_build_protocol_uri_from__uri_when__uri_isnull() {
        String uProtocolUri = UriSerializer.LONG.serialize(null);
        assertEquals("", uProtocolUri);
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from an empty  URI Object")
    public void test_build_protocol_uri_from__uri_when__uri_isEmpty() {
        UUri Uri = UUri.empty();
        String uProtocolUri = UriSerializer.LONG.serialize(Uri);
        assertEquals("", uProtocolUri);
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from an  URI object with an empty USE")
    public void test_build_protocol_uri_from__uri_when__uri_has_empty_use() {
        UEntity use = UEntity.empty();
        UUri Uri = new UUri(UAuthority.local(), use, UResource.longFormat("door"));
        String uProtocolUri = UriSerializer.LONG.serialize(Uri);
        assertEquals("/", uProtocolUri);
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from an  URI Object with a local authority with service no version")
    public void test_build_protocol_uri_from__uri_when__uri_has_local_authority_service_no_version() {
        UEntity use = UEntity.longFormat("body.access");
        UUri Uri = new UUri(UAuthority.local(), use, UResource.empty());
        String uProtocolUri = UriSerializer.LONG.serialize(Uri);
        assertEquals("/body.access", uProtocolUri);
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from an  URI Object with a local authority with service and version")
    public void test_build_protocol_uri_from__uri_when__uri_has_local_authority_service_and_version() {
        UEntity use = UEntity.longFormat("body.access", 1);
        UUri Uri = new UUri(UAuthority.local(), use, UResource.empty());
        String uProtocolUri = UriSerializer.LONG.serialize(Uri);
        assertEquals("/body.access/1", uProtocolUri);
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from an  URI Object with a local authority with service no version with resource")
    public void test_build_protocol_uri_from__uri_when__uri_has_local_authority_service_no_version_with_resource() {
        UEntity use = UEntity.longFormat("body.access");
        UUri Uri = new UUri(UAuthority.local(), use, UResource.longFormat("door"));
        String uProtocolUri = UriSerializer.LONG.serialize(Uri);
        assertEquals("/body.access//door", uProtocolUri);
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from an  URI Object with a local authority with service and version with resource")
    public void test_build_protocol_uri_from__uri_when__uri_has_local_authority_service_and_version_with_resource() {
        UEntity use = UEntity.longFormat("body.access", 1);
        UUri Uri = new UUri(UAuthority.local(), use, UResource.longFormat("door"));
        String uProtocolUri = UriSerializer.LONG.serialize(Uri);
        assertEquals("/body.access/1/door", uProtocolUri);
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from an  URI Object with a local authority with service no version with resource with instance no message")
    public void test_build_protocol_uri_from__uri_when__uri_has_local_authority_service_no_version_with_resource_with_instance_no_message() {
        UEntity use = UEntity.longFormat("body.access");
        UUri Uri = new UUri(UAuthority.local(), use, UResource.longFormat("door", "front_left", null));
        String uProtocolUri = UriSerializer.LONG.serialize(Uri);
        assertEquals("/body.access//door.front_left", uProtocolUri);
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from an  URI Object with a local authority with service and version with resource with instance no message")
    public void test_build_protocol_uri_from__uri_when__uri_has_local_authority_service_and_version_with_resource_with_instance_no_message() {
        UEntity use = UEntity.longFormat("body.access", 1);
        UUri Uri = new UUri(UAuthority.local(), use, UResource.longFormat("door", "front_left", null));
        String uProtocolUri = UriSerializer.LONG.serialize(Uri);
        assertEquals("/body.access/1/door.front_left", uProtocolUri);
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from an  URI Object with a local authority with service no version with resource with instance and message")
    public void test_build_protocol_uri_from__uri_when__uri_has_local_authority_service_no_version_with_resource_with_instance_with_message() {
        UEntity use = UEntity.longFormat("body.access");
        UUri Uri = new UUri(UAuthority.local(), use, UResource.longFormat("door", "front_left", "Door"));
        String uProtocolUri = UriSerializer.LONG.serialize(Uri);
        assertEquals("/body.access//door.front_left#Door", uProtocolUri);
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from an  URI Object with a local authority with service and version with resource with instance and message")
    public void test_build_protocol_uri_from__uri_when__uri_has_local_authority_service_and_version_with_resource_with_instance_with_message() {
        UEntity use = UEntity.longFormat("body.access", 1);
        UUri Uri = new UUri(UAuthority.local(), use, UResource.longFormat("door", "front_left", "Door"));
        String uProtocolUri = UriSerializer.LONG.serialize(Uri);
        assertEquals("/body.access/1/door.front_left#Door", uProtocolUri);
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from an  URI Object with a microRemote authority with service no version")
    public void test_build_protocol_uri_from__uri_when__uri_has_remote_authority_service_no_version() {
        UEntity use = UEntity.longFormat("body.access");
        UUri Uri = new UUri(UAuthority.longRemote("VCU", "MY_CAR_VIN"), use, UResource.empty());
        String uProtocolUri = UriSerializer.LONG.serialize(Uri);
        assertEquals("//vcu.my_car_vin/body.access", uProtocolUri);
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from an  URI Object with a microRemote authority no device with domain with service no version")
    public void test_build_protocol_uri_from__uri_when__uri_has_remote_authority_no_device_with_domain_with_service_no_version() {
        UEntity use = UEntity.longFormat("body.access");
        UUri Uri = new UUri(UAuthority.longRemote("", "MY_CAR_VIN"), use, UResource.empty());
        String uProtocolUri = UriSerializer.LONG.serialize(Uri);
        assertEquals("//my_car_vin/body.access", uProtocolUri);
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from an  URI Object with a microRemote authority with service and version")
    public void test_build_protocol_uri_from__uri_when__uri_has_remote_authority_service_and_version() {
        UEntity use = UEntity.longFormat("body.access", 1);
        UUri Uri = new UUri(UAuthority.longRemote("VCU", "MY_CAR_VIN"), use, UResource.empty());
        String uProtocolUri = UriSerializer.LONG.serialize(Uri);
        assertEquals("//vcu.my_car_vin/body.access/1", uProtocolUri);
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from an  URI Object with a microRemote cloud authority with service and version")
    public void test_build_protocol_uri_from__uri_when__uri_has_remote_cloud_authority_service_and_version() {
        UEntity use = UEntity.longFormat("body.access", 1);
        UUri Uri = new UUri(UAuthority.longRemote("cloud", "uprotocol.example.com"), use, UResource.empty());
        String uProtocolUri = UriSerializer.LONG.serialize(Uri);
        assertEquals("//cloud.uprotocol.example.com/body.access/1", uProtocolUri);
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from an  URI Object with a microRemote authority with service and version with resource")
    public void test_build_protocol_uri_from__uri_when__uri_has_remote_authority_service_and_version_with_resource() {
        UEntity use = UEntity.longFormat("body.access", 1);
        UUri Uri = new UUri(UAuthority.longRemote("VCU", "MY_CAR_VIN"), use, UResource.longFormat("door"));
        String uProtocolUri = UriSerializer.LONG.serialize(Uri);
        assertEquals("//vcu.my_car_vin/body.access/1/door", uProtocolUri);
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from an  URI Object with a microRemote authority with service no version with resource")
    public void test_build_protocol_uri_from__uri_when__uri_has_remote_authority_service_no_version_with_resource() {
        UEntity use = UEntity.longFormat("body.access");
        UUri Uri = new UUri(UAuthority.longRemote("VCU", "MY_CAR_VIN"), use, UResource.longFormat("door"));
        String uProtocolUri = UriSerializer.LONG.serialize(Uri);
        assertEquals("//vcu.my_car_vin/body.access//door", uProtocolUri);
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from an  URI Object with a microRemote authority with service and version with resource with instance no message")
    public void test_build_protocol_uri_from__uri_when__uri_has_remote_authority_service_and_version_with_resource_with_instance_no_message() {
        UEntity use = UEntity.longFormat("body.access", 1);
        UUri Uri = new UUri(UAuthority.longRemote("VCU", "MY_CAR_VIN"), use, UResource.longFormat("door", "front_left", null));
        String uProtocolUri = UriSerializer.LONG.serialize(Uri);
        assertEquals("//vcu.my_car_vin/body.access/1/door.front_left", uProtocolUri);
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from an  URI Object with a microRemote cloud authority with service and version with resource with instance no message")
    public void test_build_protocol_uri_from__uri_when__uri_has_remote_cloud_authority_service_and_version_with_resource_with_instance_no_message() {
        UEntity use = UEntity.longFormat("body.access", 1);
        UUri Uri = new UUri(UAuthority.longRemote("cloud", "uprotocol.example.com"), use, UResource.longFormat("door", "front_left", null));
        String uProtocolUri = UriSerializer.LONG.serialize(Uri);
        assertEquals("//cloud.uprotocol.example.com/body.access/1/door.front_left", uProtocolUri);
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from an  URI Object with a microRemote authority with service no version with resource with instance no message")
    public void test_build_protocol_uri_from__uri_when__uri_has_remote_authority_service_no_version_with_resource_with_instance_no_message() {
        UEntity use = UEntity.longFormat("body.access");
        UUri Uri = new UUri(UAuthority.longRemote("VCU", "MY_CAR_VIN"), use, UResource.longFormat("door", "front_left", null));
        String uProtocolUri = UriSerializer.LONG.serialize(Uri);
        assertEquals("//vcu.my_car_vin/body.access//door.front_left", uProtocolUri);
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from an  URI Object with a microRemote authority with service and version with resource with instance and message")
    public void test_build_protocol_uri_from__uri_when__uri_has_remote_authority_service_and_version_with_resource_with_instance_and_message() {
        UEntity use = UEntity.longFormat("body.access", 1);
        UUri Uri = new UUri(UAuthority.longRemote("VCU", "MY_CAR_VIN"), use, UResource.longFormat("door", "front_left", "Door"));
        String uProtocolUri = UriSerializer.LONG.serialize(Uri);
        assertEquals("//vcu.my_car_vin/body.access/1/door.front_left#Door", uProtocolUri);
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from an  URI Object with a microRemote authority with service no version with resource with instance and message")
    public void test_build_protocol_uri_from__uri_when__uri_has_remote_authority_service_no_version_with_resource_with_instance_and_message() {
        UEntity use = UEntity.longFormat("body.access");
        UUri Uri = new UUri(UAuthority.longRemote("VCU", "MY_CAR_VIN"), use, UResource.longFormat("door", "front_left", "Door"));
        String uProtocolUri = UriSerializer.LONG.serialize(Uri);
        assertEquals("//vcu.my_car_vin/body.access//door.front_left#Door", uProtocolUri);
    }

    @Test
    @DisplayName("Test Create a uProtocol URI for the source part of an RPC request, where the source is local")
    public void test_build_protocol_uri_for_source_part_of_rpc_request_where_source_is_local() {
        UAuthority uAuthority = UAuthority.local();
        UEntity use = UEntity.longFormat("petapp", 1);
        String uProtocolUri = UriSerializer.LONG.serialize(UUri.rpcResponse(uAuthority, use));
        assertEquals("/petapp/1/rpc.response", uProtocolUri);
    }

    @Test
    @DisplayName("Test Create a uProtocol URI for the source part of an RPC request, where the source is microRemote")
    public void test_build_protocol_uri_for_source_part_of_rpc_request_where_source_is_remote() {
        UAuthority uAuthority = UAuthority.longRemote("cloud", "uprotocol.example.com");
        UEntity use = UEntity.longFormat("petapp");
        String uProtocolUri = UriSerializer.LONG.serialize(UUri.rpcResponse(uAuthority, use));
        assertEquals("//cloud.uprotocol.example.com/petapp//rpc.response", uProtocolUri);
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from parts that are null")
    public void test_build_protocol_uri_from_parts_when_they_are_null() {
        UAuthority uAuthority = null;
        UEntity uSoftwareEntity = null;
        UResource uResource = null;
        UUri Uri = new UUri(uAuthority, uSoftwareEntity, uResource);
        String uProtocolUri = UriSerializer.LONG.serialize(Uri);
        assertEquals("", uProtocolUri);
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from the parts of  URI Object with a microRemote authority with service and version with resource")
    public void test_build_protocol_uri_from__uri_parts_when__uri_has_remote_authority_service_and_version_with_resource() {
        UAuthority uAuthority = UAuthority.longRemote("VCU", "MY_CAR_VIN");
        UEntity use = UEntity.longFormat("body.access", 1);
        UResource uResource = UResource.longFormat("door");
        String uProtocolUri = UriSerializer.LONG.serialize(new UUri(uAuthority, use, uResource));
        assertEquals("//vcu.my_car_vin/body.access/1/door", uProtocolUri);
    }

    @Test
    @DisplayName("Test Create a URI using no scheme")
    public void test_custom_scheme_no_scheme_empty() {
        UAuthority uAuthority = null;
        UEntity uSoftwareEntity = null;
        UResource uResource = null;
        String customUri = UriSerializer.LONG.serialize(new UUri(uAuthority, uSoftwareEntity, uResource));
        assertTrue(customUri.isEmpty());
    }

    @Test
    @DisplayName("Test Create a custom URI using no scheme")
    public void test_custom_scheme_no_scheme() {
        UAuthority uAuthority = UAuthority.longRemote("VCU", "MY_CAR_VIN");
        UEntity use = UEntity.longFormat("body.access", 1);
        UResource uResource = UResource.longFormat("door");
        String ucustomUri = UriSerializer.LONG.serialize(new UUri(uAuthority, use, uResource));
        assertEquals("//vcu.my_car_vin/body.access/1/door", ucustomUri);
    }

    @Test
    @DisplayName("Test parse local uProtocol uri with custom scheme")
    public void test_parse_local_protocol_uri_with_custom_scheme() {
        String uri = "custom:/body.access//door.front_left#Door";
        UUri Uri = UriSerializer.LONG.deserialize(uri);
        assertTrue(Uri.uAuthority().isLocal());
        assertFalse(Uri.uAuthority().isMarkedRemote());
        assertEquals("body.access", Uri.uEntity().name());
        assertTrue(Uri.uEntity().version().isEmpty());
        assertEquals("door", Uri.uResource().name());
        assertTrue(Uri.uResource().instance().isPresent());
        assertEquals("front_left", Uri.uResource().instance().get());
        assertTrue(Uri.uResource().message().isPresent());
        assertEquals("Door", Uri.uResource().message().get());
    }

    @Test
    @DisplayName("Test parse microRemote uProtocol uri with custom scheme")
    public void test_parse_remote_protocol_uri_with_custom_scheme() {
        String uri = "custom://vcu.vin/body.access//door.front_left#Door";
        String uri2 = "//vcu.vin/body.access//door.front_left#Door";
        UUri Uri = UriSerializer.LONG.deserialize(uri);
        assertFalse(Uri.uAuthority().isLocal());
        assertTrue(Uri.uAuthority().isMarkedRemote());
        assertEquals("vcu", Uri.uAuthority().device().get());
        assertTrue(Uri.uAuthority().domain().isPresent());
        assertEquals("vin", Uri.uAuthority().domain().get());
        assertEquals("body.access", Uri.uEntity().name());
        assertTrue(Uri.uEntity().version().isEmpty());
        assertEquals("door", Uri.uResource().name());
        assertTrue(Uri.uResource().instance().isPresent());
        assertEquals("front_left", Uri.uResource().instance().get());
        assertTrue(Uri.uResource().message().isPresent());
        assertEquals("Door", Uri.uResource().message().get());
        assertEquals(uri2, UriSerializer.LONG.serialize(Uri));
    }

}
