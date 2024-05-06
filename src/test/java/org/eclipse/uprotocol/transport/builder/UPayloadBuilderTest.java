package org.eclipse.uprotocol.transport.builder;

import org.eclipse.uprotocol.v1.UAttributes;
import org.eclipse.uprotocol.v1.UPayload;
import org.eclipse.uprotocol.v1.UPayloadFormat;
import org.eclipse.uprotocol.v1.UUri;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.protobuf.Any;

import io.cloudevents.v1.proto.CloudEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

public class UPayloadBuilderTest {
    
    @Test
    @DisplayName("Test pack to any")
    public void test_pack_to_any() {
        CloudEvent message = CloudEvent.newBuilder()
                .setId("myId")
                .setSource("mySource")
                .setType("myType")
                .build();
        UPayload payload = UPayloadBuilder.packToAny(message);
        assertTrue(payload.getFormat() == UPayloadFormat.UPAYLOAD_FORMAT_PROTOBUF_WRAPPED_IN_ANY);
        try {
            Any any = Any.parseFrom(payload.getData());
            CloudEvent unpacked = any.unpack(CloudEvent.class);
            assertEquals(message, unpacked);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("Test pack")
    public void test_pack() {
        CloudEvent message = CloudEvent.newBuilder()
                .setId("myId")
                .setSource("mySource")
                .setType("myType")
                .build();
        UPayload payload = UPayloadBuilder.pack(message);
        assertTrue(payload.getFormat() == UPayloadFormat.UPAYLOAD_FORMAT_PROTOBUF);
        try {
            CloudEvent unpacked = CloudEvent.parseFrom(payload.getData());
            assertEquals(message, unpacked);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("Test unpack")
    public void test_unpack() {
        CloudEvent message = CloudEvent.newBuilder()
                .setId("myId")
                .setSource("mySource")
                .setType("myType")
                .build();
        UPayload payload = UPayload.newBuilder()
                .setFormat(UPayloadFormat.UPAYLOAD_FORMAT_PROTOBUF)
                .setData(message.toByteString())
                .build();
        Optional<CloudEvent> unpacked = UPayloadBuilder.unpack(payload, CloudEvent.class);
        assertTrue(unpacked.isPresent());

        assertEquals(message, unpacked.get());
    }

    @Test
    @DisplayName("Test unpack empty payload")
    public void test_unpack_empty_payload() {
        UPayload payload = UPayload.newBuilder().build();
        Optional<CloudEvent> unpacked = UPayloadBuilder.unpack(payload, CloudEvent.class);
        assertTrue(unpacked.isEmpty());
    }

    @Test
    @DisplayName("Test unpack null payload")
    public void test_unpack_null_payload() {
        Optional<CloudEvent> unpacked = UPayloadBuilder.unpack(null, CloudEvent.class);
        assertTrue(unpacked.isEmpty());
    }

    @Test
    @DisplayName("Test unpack payload without format being set")
    public void test_unpack_payload_without_format() {
        CloudEvent message = CloudEvent.newBuilder()
                .setId("myId")
                .setSource("mySource")
                .setType("myType")
                .build();
        UPayload payload = UPayload.newBuilder()
                .setData(Any.pack(message).toByteString())
                .build();
        Optional<CloudEvent> unpacked = UPayloadBuilder.unpack(payload, CloudEvent.class);
        assertEquals(payload.getFormat(), UPayloadFormat.UPAYLOAD_FORMAT_UNSPECIFIED);
        assertTrue(unpacked.isPresent());
        assertEquals(message, unpacked.get());
    }
    
    @Test
    @DisplayName("Test unpack payload without the wrong class type")
    public void test_unpack_payload_without_wrong_class_type() {
        CloudEvent message = CloudEvent.newBuilder()
                .setId("myId")
                .setSource("mySource")
                .setType("myType")
                .build();
        UPayload payload = UPayloadBuilder.pack(message);
        Optional<UAttributes> unpacked = UPayloadBuilder.unpack(payload, UAttributes.class);
        assertTrue(unpacked.isEmpty());
    }

    @Test
    @DisplayName("Test unpack payload without an unsupported format")
    public void test_unpack_payload_without_unsupported_format() {
        CloudEvent message = CloudEvent.newBuilder()
                .setId("myId")
                .setSource("mySource")
                .setType("myType")
                .build();
        UPayload payload = UPayload.newBuilder()
                .setFormat(UPayloadFormat.UPAYLOAD_FORMAT_SOMEIP)
                .setData(Any.pack(message).toByteString())
                .build();
        Optional<CloudEvent> unpacked = UPayloadBuilder.unpack(payload, CloudEvent.class);
        assertTrue(unpacked.isEmpty());
    }

}
