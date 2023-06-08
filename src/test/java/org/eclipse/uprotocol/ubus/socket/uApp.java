package org.eclipse.uprotocol.ubus.socket;

import java.io.IOException;
import java.net.UnknownHostException;

import org.eclipse.uprotocol.cloudevent.datamodel.UCloudEventAttributes;
import org.eclipse.uprotocol.ubus.EventListener;
import org.eclipse.uprotocol.uri.datamodel.UAuthority;
import org.eclipse.uprotocol.uri.datamodel.UEntity;
import org.eclipse.uprotocol.uri.datamodel.UResource;
import org.eclipse.uprotocol.uri.datamodel.UUri;

import com.google.protobuf.Any;
import com.google.rpc.Status;

public class uApp {
    
    private class AppEventListener implements EventListener {

        @Override
        public Status onEvent(UUri topic, byte[] data, UCloudEventAttributes attributes) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'onEvent'");
        }

    };
    
    public static void main(String[] args) throws UnknownHostException, IOException, ClassNotFoundException, InterruptedException {
        UEntity use = new UEntity("HartleyApp", "1.0");
        AppEventListener listener;

        SocketSubscriber subscriber = new SocketSubscriber(use);
        //subscriber.registerEventListener(null, listener);
        
        /* Block and wait */
    }

}
