package org.eclipse.uprotocol.uri.builder;

import org.eclipse.uprotocol.UprotocolOptions;
import org.eclipse.uprotocol.v1.UEntity;

import com.google.protobuf.DescriptorProtos.ServiceOptions;
import com.google.protobuf.Descriptors.ServiceDescriptor;

public interface UEntityBuilder {
    static UEntity fromProto(ServiceDescriptor descriptor) {
        if (descriptor == null) {
            return UEntity.getDefaultInstance();
        }

        ServiceOptions options = descriptor.getOptions();

        return UEntity.newBuilder()
            .setName(options.<String>getExtension(UprotocolOptions.name))
            .setId(options.<Integer>getExtension(UprotocolOptions.id))
            .setVersionMajor(options.<Integer>getExtension(UprotocolOptions.versionMajor))
            .build();
    }
}
