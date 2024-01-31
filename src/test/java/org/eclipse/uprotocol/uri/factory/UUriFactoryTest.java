package org.eclipse.uprotocol.uri.factory;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.uprotocol.core.usubscription.v3.USubscriptionProto;
import org.eclipse.uprotocol.core.usubscription.v3.Update;
import org.eclipse.uprotocol.core.usubscription.v3.Update.Resources;
import org.eclipse.uprotocol.v1.UUri;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.protobuf.Descriptors.ServiceDescriptor;

public class UUriFactoryTest {
    @Test
    @DisplayName("Test build valid usubscription notification topic")
    public void test_build_valid_usubscription_uentity() {
        ServiceDescriptor descriptor = USubscriptionProto.getDescriptor().getServices().get(0);
        UUri uri = UUriFactory.fromProto(descriptor, 1000);

        assertEquals(uri.getEntity().getName(), "core.usubscription");
        assertEquals(uri.getEntity().getId(), 0);
        assertEquals(uri.getEntity().getVersionMajor(), 3);
        assertEquals(uri.getEntity().getVersionMinor(), 0);
        assertEquals(uri.getResource().getId(), 1000);
        assertEquals(uri.getResource().getMessage(), Update.getDescriptor().getName());
        assertEquals(uri.getResource().getName(), Resources.getDescriptor().findValueByNumber(Resources.subscriptions_VALUE).getName());
    }
}
