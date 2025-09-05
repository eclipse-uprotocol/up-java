package org.eclipse.uprotocol.uri.factory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Stream;

import org.eclipse.uprotocol.Uoptions;
import org.eclipse.uprotocol.core.usubscription.v3.USubscriptionProto;
import org.eclipse.uprotocol.v1.UUri;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.ServiceDescriptorProto;
import com.google.protobuf.DescriptorProtos.ServiceOptions;
import com.google.protobuf.Descriptors.DescriptorValidationException;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.Descriptors.ServiceDescriptor;

class UriFactoryTest {

    @Test
    @DisplayName("Test ANY")
    void testAny() {
        final UUri uri = UriFactory.ANY;

        assertEquals("*", uri.getAuthorityName());
        assertEquals(0xFFFF_FFFF, uri.getUeId());
        assertEquals(0xFF, uri.getUeVersionMajor());
        assertEquals(0xFFFF, uri.getResourceId());
    }

    static ServiceDescriptor createServiceDescriptor(OptionalInt serviceId, OptionalInt majorVersion) {
        var optionsBuilder = ServiceOptions.newBuilder();
        serviceId.ifPresent(id -> optionsBuilder.setExtension(Uoptions.serviceId, id));
        majorVersion.ifPresent(version -> optionsBuilder.setExtension(Uoptions.serviceVersionMajor, version));
        ServiceDescriptorProto serviceProto = ServiceDescriptorProto.newBuilder()
            .setName("TestService")
            .setOptions(optionsBuilder)
            .build();

        FileDescriptorProto fileProto = FileDescriptorProto.newBuilder()
            .setName("test.proto")
            .setPackage("test")
            .addService(serviceProto)
            .build();

        try {
            var fileDescriptor = FileDescriptor.buildFrom(fileProto, new FileDescriptor[0]);
            return fileDescriptor.getServices().get(0);
        } catch (DescriptorValidationException e) {
            throw new IllegalArgumentException("cannot create ServiceDescriptor for arguments", e);
        }
    }

    static Stream<Arguments> fromProtoProvider() {
        var uSubscriptionDesc = USubscriptionProto.getDescriptor().getServices().get(0);
        return Stream.of(
            Arguments.of(uSubscriptionDesc, 0, "*", null),
            Arguments.of(uSubscriptionDesc, 1, "hartley", null),
            Arguments.of(uSubscriptionDesc, 2, null, null),
            Arguments.of(uSubscriptionDesc, -1, null, IllegalArgumentException.class),
            Arguments.of(
                createServiceDescriptor(OptionalInt.empty(), OptionalInt.empty()),
                1,
                "localhost",
                IllegalArgumentException.class),
            Arguments.of(
                createServiceDescriptor(OptionalInt.of(0x0010_a1bf), OptionalInt.empty()),
                1,
                "localhost",
                IllegalArgumentException.class),
            Arguments.of(
                createServiceDescriptor(OptionalInt.empty(), OptionalInt.of(0x02)),
                1,
                "localhost",
                IllegalArgumentException.class)
        );
    }

    @ParameterizedTest(name = "Test fromProto: {index} - {arguments}")
    @MethodSource("fromProtoProvider")
    public void testFromProtoWithParameters(
            ServiceDescriptor descriptor,
            int resourceId,
            String authorityName,
            Class<Exception> expectedOutcome) {
        if (expectedOutcome != null) {
            assertThrows(
                expectedOutcome,
                () -> UriFactory.fromProto(descriptor, resourceId, authorityName));
        } else {
            final UUri uri = UriFactory.fromProto(descriptor, resourceId, authorityName);
            assertEquals(Optional.ofNullable(authorityName).orElse(""), uri.getAuthorityName());
            assertEquals(0, uri.getUeId());
            assertEquals(3, uri.getUeVersionMajor());
            assertEquals(resourceId, uri.getResourceId());
        }
    }
}
