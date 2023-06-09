package org.eclipse.uprotocol.cloudevent.factory;

import java.util.Optional;

import org.eclipse.uprotocol.cloudevent.datamodel.MicroCloudEvent;
import org.eclipse.uprotocol.cloudevent.datamodel.MicroCloudEvent.Priority;
import org.eclipse.uprotocol.uri.datamodel.UUri;

public interface MicroCloudEventFactory {

    static MicroCloudEvent publish(UUri source, byte[] data, Optional<Priority> priority, Optional<Integer> ttl,
        Optional<String> schema, Optional<String> contenttype) {
        
        return buildBaseMicroCloudEvent(source, data, priority, ttl, schema, contenttype)
            .withType(MicroCloudEvent.Type.PUBLISH)
            .build();
    }


    static MicroCloudEvent notify(UUri source, UUri sink, byte[] data, Optional<Priority> priority, Optional<Integer> ttl,
        Optional<String> schema, Optional<String> contenttype) {
        
        return buildBaseMicroCloudEvent(source, data, priority, ttl, schema, contenttype)
            .withType(MicroCloudEvent.Type.NOTIFICATION)
            .withSink(sink)
            .build();
    }


    static MicroCloudEvent request(UUri request, UUri response, byte[] data, Optional<Priority> priority, Optional<Integer> ttl,
        Optional<String> schema, Optional<String> contenttype) {
        
        return buildBaseMicroCloudEvent(request, data, priority, ttl, schema, contenttype)
            .withType(MicroCloudEvent.Type.REQUEST)
            .withSink(response)
            .build();
    }


    static MicroCloudEvent response(UUri request, UUri response, byte[] data, Optional<Priority> priority, Optional<Integer> ttl,
        Optional<String> schema, Optional<String> contenttype) {
        
        return buildBaseMicroCloudEvent(request, data, priority, ttl, schema, contenttype)
            .withType(MicroCloudEvent.Type.REQUEST)
            .withSink(response)
            .build();
    }

    static MicroCloudEvent.MicroCloudEventBuilder buildBaseMicroCloudEvent(UUri source, byte[] data, 
        Optional<Priority> p, Optional<Integer> timeout,
        Optional<String> schema, Optional<String> contenttype) {

        final MicroCloudEvent.MicroCloudEventBuilder builder = new MicroCloudEvent.MicroCloudEventBuilder()
            .withData(data)
            .withSource(source);
        
            p.ifPresent(priority -> builder.withPriority(priority));
            timeout.ifPresent(ttl -> builder.withTtl(ttl));
            schema.ifPresent(dataschema -> builder.withDataSchema(dataschema));
            contenttype.ifPresent(datacontenttype -> builder.withDataContentType(datacontenttype));

        return builder;
    }
   
}
