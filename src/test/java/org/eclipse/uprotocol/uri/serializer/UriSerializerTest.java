package org.eclipse.uprotocol.uri.serializer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.uprotocol.uri.datamodel.UAuthority;
import org.eclipse.uprotocol.uri.datamodel.UEntity;
import org.eclipse.uprotocol.uri.datamodel.UResource;
import org.eclipse.uprotocol.uri.datamodel.UUri;
public class UriSerializerTest {

    @Test
    @DisplayName("Test using the serializers")
    public void test_using_the_serializers() {
        final UUri uri = new UUri(UAuthority.local(), UEntity.fromName("hartley"), UResource.forRpc("raise"));
        final String strUri = UriSerializer.LONG.serialize(uri);
        assertEquals("/hartley//rpc.raise", strUri);
        final UUri uri2 = UriSerializer.LONG.deserialize(strUri);
        assertTrue(uri.equals(uri2));
        

    }

}
