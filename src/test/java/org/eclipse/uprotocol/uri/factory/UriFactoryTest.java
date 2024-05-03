package org.eclipse.uprotocol.uri.factory;

import static org.junit.jupiter.api.Assertions.assertEquals;


import org.eclipse.uprotocol.core.usubscription.v3.USubscriptionProto;
import org.eclipse.uprotocol.v1.UUri;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class UriFactoryTest {
    
    @Test
    @DisplayName("Test fromProto")
    public void testFromProto() {
        final UUri uri = UriFactory.fromProto(
            USubscriptionProto.getDescriptor().getServices().get(0), 0);

        assertEquals(uri.getAuthorityName(), "");
        assertEquals(uri.getUeId(), 0);
        assertEquals(uri.getUeVersionMajor(), 3);
        assertEquals(uri.getResourceId(), 0);
    }

    @Test
    @DisplayName("Test ANY")
    public void testAny() {
        final UUri uri = UriFactory.ANY;

        assertEquals(uri.getAuthorityName(), "*");
        assertEquals(uri.getUeId(), 65535);
        assertEquals(uri.getUeVersionMajor(), 255);
        assertEquals(uri.getResourceId(), 65535);
    }

    @Test
    @DisplayName("Test fromProto with null descriptor")
    public void testFromProtoWithNullDescriptor() {
        final UUri uri = UriFactory.fromProto(null, 0);

        assertEquals(uri.getAuthorityName(), "");
        assertEquals(uri.getUeId(), 0);
        assertEquals(uri.getUeVersionMajor(), 0);
        assertEquals(uri.getResourceId(), 0);
    }

    @Test
    @DisplayName("Test fromProto with authority name")
    public void testFromProtoWithAuthorityName() {
        final UUri uri = UriFactory.fromProto(
            USubscriptionProto.getDescriptor().getServices().get(0), 0, "hartley");

        assertEquals(uri.getAuthorityName(), "hartley");
        assertEquals(uri.getUeId(), 0);
        assertEquals(uri.getUeVersionMajor(), 3);
        assertEquals(uri.getResourceId(), 0);
    }

    @Test
    @DisplayName("Test fromProto with empty authority name string")
    public void testFromProtoWithEmptyAuthorityName() {
        final UUri uri = UriFactory.fromProto(
            USubscriptionProto.getDescriptor().getServices().get(0), 0, "");

        assertEquals(uri.getAuthorityName(), "");
        assertEquals(uri.getUeId(), 0);
        assertEquals(uri.getUeVersionMajor(), 3);
        assertEquals(uri.getResourceId(), 0);
    }
}
