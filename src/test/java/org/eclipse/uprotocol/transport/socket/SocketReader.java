package org.eclipse.uprotocol.transport.socket;

import java.io.IOException;
import java.io.ObjectInputStream;

import javax.annotation.Nonnull;

import org.eclipse.uprotocol.transport.MessageReceiver;

public class SocketReader implements Runnable {

    private final ObjectInputStream ois;
    private final MessageReceiver receiver;

    public SocketReader(
        @Nonnull ObjectInputStream _ois, 
        @Nonnull MessageReceiver _receiver) {
        ois = _ois;
        receiver = _receiver;
    }

    @Override
    public void run() {
        try {
            while (true) {
                receiver.onReceive((byte[])ois.readObject());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
