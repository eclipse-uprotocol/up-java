package org.eclipse.uprotocol.transport;

import java.util.ArrayList;

import com.google.rpc.Status;

import io.cloudevents.CloudEvent;


/** uProtocol Transport Layer Interface (uP-l1)
 * Interface is to be implemented by the various transport technologies (ex. MQTT, Binder, HTTP, etc...)
 * NOTE: SW developers do not call these APIs, they interract with the boundary object ULink.java
 */
public interface Transport {
    /**
	 * Send a CloudEvent to the connected uE
     * The send command returns immediately and means that your request is valid and will
	 * be sent to the platform or it is not valid and not sent on.
	 * @param ce  Cloudevent to send
	 * @return Status The result from the send()
	 */
	Status send(CloudEvent cloudEvent);
  

     /** Register Receier
     * When the transport supports push type delivery method, the caller invokes this
	 * method to register a listener to receive the events 
	 * @param receiver The message reciver
	 * @return Status The result from the send()
	 */
	Status registerReceiver(Receiver receiver);


    /**
	 * Receive CloudEvent messages
     * API to fetch 0 to n messages from the sender used when the transport supports 
	 * @return ReceiveResult that contains the google.rpc.status and a batch of cloudevents
	 */
	ArrayList<CloudEvent> Receive();
}
