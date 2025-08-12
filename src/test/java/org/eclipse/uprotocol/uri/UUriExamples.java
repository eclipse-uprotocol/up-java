package org.eclipse.uprotocol.uri;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.uprotocol.core.usubscription.v3.USubscriptionProto;
import org.eclipse.uprotocol.uri.factory.UriFactory;
import org.eclipse.uprotocol.uri.serializer.UriSerializer;
import org.eclipse.uprotocol.uri.validator.UriValidator;
import org.eclipse.uprotocol.v1.UUri;
import org.junit.jupiter.api.Test;

public class UUriExamples {
   
    @Test 
    public void exampleUrifactoryFromproto() {
        // Fetch the notification topic Uri from the USubscriptionProto generated code
        final UUri uri = UriFactory.fromProto(
            USubscriptionProto.getDescriptor().getServices().get(0), 0);

        assertEquals(uri.getUeId(), 0);
        assertEquals(uri.getUeVersionMajor(), 3);
        assertEquals(uri.getResourceId(), 0);
    }

    @Test
    public void exampleSerializerDeserializer() {
        final UUri uri = UUri.newBuilder()
                .setUeId(1).setUeVersionMajor(2).setResourceId(3).build();
        final String strUri = UriSerializer.serialize(uri);
        assertEquals("/1/2/3", strUri);
        assertEquals(uri, UriSerializer.deserialize(strUri));
    }

    @Test
    public void exampleUrivalidator() {
        final UUri uri = UUri.newBuilder()
                .setUeId(1).setUeVersionMajor(2).setResourceId(3).build();
        assertFalse(UriValidator.isEmpty(uri));
        assertFalse(UriValidator.isDefaultResourceId(uri));
        assertTrue(UriValidator.isRpcMethod(uri));
        assertFalse(UriValidator.isRpcResponse(uri));
        assertFalse(UriValidator.isTopic(uri));
    }
}
