package org.eclipse.uprotocol.uri.factory;

import org.eclipse.uprotocol.TopicMetadata;
import org.eclipse.uprotocol.UprotocolOptions;
import org.eclipse.uprotocol.v1.UEntity;
import org.eclipse.uprotocol.v1.UResource;
import org.eclipse.uprotocol.v1.UUri;

import com.google.protobuf.DescriptorProtos.EnumValueOptions;
import com.google.protobuf.DescriptorProtos.ServiceOptions;
import com.google.protobuf.Descriptors.EnumValueDescriptor;
import com.google.protobuf.Descriptors.ServiceDescriptor;

public interface UUriFactory {
    static UUri fromProto(ServiceDescriptor service, EnumValueDescriptor value) {
        if (service == null || value == null) {
            return UUri.getDefaultInstance();
        }

        ServiceOptions options = service.getOptions();
        EnumValueOptions valueOptions = value.getOptions();

        UEntity entity = UEntity.newBuilder()
            .setName(options.<String>getExtension(UprotocolOptions.name))
            .setId(options.<Integer>getExtension(UprotocolOptions.id))
            .setVersionMajor(options.<Integer>getExtension(UprotocolOptions.versionMajor))
            .build();
        TopicMetadata metadata = valueOptions.getExtension(UprotocolOptions.topicMetadata);
        
            if (metadata == null) {
            return UUri.getDefaultInstance();
        }

        UResource resource = UResource.newBuilder()
            .setId(metadata.getId()) 
            .setName(value.getName())           
            .setMessage(metadata.getMessage())
            .build();    
        return UUri.newBuilder()
            .setEntity(entity)
            .setResource(resource)
            .build();
    }

}
