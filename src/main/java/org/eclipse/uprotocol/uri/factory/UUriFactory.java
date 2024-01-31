package org.eclipse.uprotocol.uri.factory;

import java.util.List;

import org.eclipse.uprotocol.UprotocolOptions;
import org.eclipse.uprotocol.v1.Topic;
import org.eclipse.uprotocol.v1.UEntity;
import org.eclipse.uprotocol.v1.UResource;
import org.eclipse.uprotocol.v1.UUri;

import com.google.protobuf.DescriptorProtos.ServiceOptions;
import com.google.protobuf.Descriptors.ServiceDescriptor;

public interface UUriFactory {
    static UUri fromProto(ServiceDescriptor descriptor, Integer topicId) {
        if (descriptor == null) {
            return UUri.getDefaultInstance();
        }

        ServiceOptions options = descriptor.getOptions();

        UEntity entity = UEntity.newBuilder()
            .setName(options.<String>getExtension(UprotocolOptions.name))
            .setId(options.<Integer>getExtension(UprotocolOptions.id))
            .setVersionMajor(options.<Integer>getExtension(UprotocolOptions.versionMajor))
            .build();
        Topic topic = options.getExtension(UprotocolOptions.topic).stream()
            .filter(t -> t.getId() == topicId)
            .findFirst()
            .orElse(null);
        
            if (topic == null) {
            return UUri.getDefaultInstance();
        }

        UResource resource = UResource.newBuilder()
            .setId(topic.getId()) 
            .setName(topic.getResources(0))           
            .setMessage(topic.getMessage())
            .setInstance(topic.getResources(0))
            .build();    
        return UUri.newBuilder()
            .setEntity(entity)
            .setResource(resource)
            .build();
    }

}
