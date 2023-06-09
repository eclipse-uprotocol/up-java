package org.eclipse.uprotocol.transport.echo;

import org.eclipse.uprotocol.status.datamodel.UStatus;
import org.eclipse.uprotocol.status.factory.UStatusFactory;
import org.eclipse.uprotocol.transport.UMessageReceiver;

import io.cloudevents.CloudEvent;

public class EchoMessageReceiver implements UMessageReceiver {

    @Override
    public UStatus onReceive(CloudEvent ce) {
        System.out.println("Received: " + ce.toString());
        return UStatusFactory.buildOkUStatus();
    }
  
}
