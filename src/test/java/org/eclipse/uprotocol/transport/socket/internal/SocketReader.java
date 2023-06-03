package org.eclipse.uprotocol.transport.socket.internal;

import java.io.IOException;
import java.io.ObjectInputStream;

import javax.annotation.Nonnull;

import org.eclipse.uprotocol.cloudevent.serialize.CloudEventSerializer;
import org.eclipse.uprotocol.transport.Receiver;

public class SocketReader implements Runnable {

    private final ObjectInputStream ois;
    private final Receiver receiver;
    private final CloudEventSerializer serializer;

    public SocketReader(
        @Nonnull ObjectInputStream _ois, 
        @Nonnull Receiver _receiver, 
        @Nonnull CloudEventSerializer _serializer) {
        ois = _ois;
        receiver = _receiver;
        serializer = _serializer;
    }

    @Override
    public void run() {
        try {
            while (true) {
                receiver.onReceive(serializer.deserialize((byte[])ois.readObject()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
