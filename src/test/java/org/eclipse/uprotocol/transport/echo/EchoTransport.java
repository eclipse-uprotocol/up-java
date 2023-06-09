package org.eclipse.uprotocol.transport.echo;

import javax.annotation.Nonnull;

import org.eclipse.uprotocol.cloudevent.datamodel.UCloudEventAttributes;
import org.eclipse.uprotocol.cloudevent.factory.CloudEventFactory;

import static org.eclipse.uprotocol.cloudevent.factory.UCloudEvent.getSink;
import static org.eclipse.uprotocol.cloudevent.factory.UCloudEvent.getSource;
import static org.eclipse.uprotocol.cloudevent.factory.UCloudEvent.getPayload;
import static org.eclipse.uprotocol.cloudevent.factory.UCloudEvent.getUCloudEventAttributes;

import org.eclipse.uprotocol.status.datamodel.UStatus;
import org.eclipse.uprotocol.status.factory.UStatusFactory;
import org.eclipse.uprotocol.transport.UMessageReceiver;
import org.eclipse.uprotocol.transport.UTransport;
import org.eclipse.uprotocol.uri.datamodel.UAuthority;
import org.eclipse.uprotocol.uri.datamodel.UEntity;
import org.eclipse.uprotocol.uri.datamodel.UResource;
import org.eclipse.uprotocol.uri.datamodel.UUri;
import org.eclipse.uprotocol.uri.factory.UriFactory;

import com.google.protobuf.Any;

import io.cloudevents.CloudEvent;

/**
 * Echo transport will loopback whatever it is sent
 */
public class EchoTransport implements UTransport {

    private final UMessageReceiver receiver;

    public EchoTransport(@Nonnull UMessageReceiver receiver) {
    this.receiver = receiver;
    }


    /**
     * Push delivery test using an echo from a send.
     */
    @Override
    public UStatus send(CloudEvent ce) {
        System.out.println("Echoing back sent CE: " + ce.toString());
        CloudEvent echo = CloudEventFactory.buildBaseCloudEvent(
            CloudEventFactory.generateCloudEventId(), 
            getSink(ce).orElse("null"), getPayload(ce).toByteArray(), getPayload(ce).getTypeUrl(), getUCloudEventAttributes(ce))
            .withExtension("sink", getSource(ce))
            .withType(ce.getType())
            .build();
        receiver.onReceive(echo);
        
        return UStatusFactory.buildOkUStatus();
    }


    /**
     * Pull delivery test with generating a fake cloudevent
     */
    @Override
    public CloudEvent receive() {
        UUri topic = new UUri(UAuthority.local(), new UEntity("HartleyUBus", "1.0"),
        new UResource("pull", "delivery", "method"));

        CloudEvent ce = CloudEventFactory.publish(
            UriFactory.buildUProtocolUri(topic), 
            Any.newBuilder().build(), 
            new UCloudEventAttributes.UCloudEventAttributesBuilder()
                .withHash("somehash")
                .withPriority(UCloudEventAttributes.Priority.OPERATIONS)
                .withTtl(3)
                .withToken("someOAuthToken")
                .build());
        System.out.println("Fake received CE: " + ce.toString());
        return ce;
    }
}
