package org.eclipse.uprotocol.transport;
import io.cloudevents.*;

// Interface to define a receive listener used for transports that 
// support push type delivery method. 
public interface Receiver {
	// Receive Message Handler
	// When messages are received, this function is called.
	// @param ce CloudEvent received
	// @return void
	void onReceive(CloudEvent[] ces);
}