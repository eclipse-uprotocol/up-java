package org.eclipse.uprotocol.ubusclient;

import org.eclipse.uprotocol.Ack;
import org.eclipse.uprotocol.umessage.UMessage;
import org.eclipse.uprotocol.topic.UEntity;
import org.eclipse.uprotocol.topic.UTopic;

import java.util.concurrent.CompletionStage;

public interface UBusClient {

    Ack connect(UEntity<?> uEntity);

    CompletionStage<Ack> publish(UMessage uMessage);

    CompletionStage<Ack> invokeMethod(UMessage method);

    CompletionStage<Ack> registerListener(UTopic<?,?> topic);

}
