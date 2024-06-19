package org.eclipse.uprotocol.communication;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;

import org.eclipse.uprotocol.transport.UListener;
import org.eclipse.uprotocol.transport.UTransport;
import org.eclipse.uprotocol.transport.builder.UMessageBuilder;
import org.eclipse.uprotocol.transport.validate.UAttributesValidator;
import org.eclipse.uprotocol.v1.UCode;
import org.eclipse.uprotocol.v1.UMessage;
import org.eclipse.uprotocol.v1.UMessageType;
import org.eclipse.uprotocol.v1.UStatus;
import org.eclipse.uprotocol.v1.UUri;
import org.eclipse.uprotocol.validation.ValidationResult;

/**
 * TestUTransport is a test implementation of the UTransport interface 
 * that can only hold a single listener for testing.
 */
public class TestUTransport implements UTransport {

    protected List<UListener> listeners = new CopyOnWriteArrayList<>();
    private final UUri mSource;

    public UMessage buildResponse(UMessage request) {
        return UMessageBuilder.response(request.getAttributes())
            .build(UPayload.pack(request.getPayload(), request.getAttributes().getPayloadFormat()));
    }

    public TestUTransport() {
        this(UUri.newBuilder()
            .setAuthorityName("Hartley").setUeId(4).setUeVersionMajor(1).build());
    }

    public TestUTransport(UUri source) {
        mSource = source;
    }

    @Override
    public UStatus send(UMessage message) {
        UAttributesValidator validator = UAttributesValidator.getValidator(message.getAttributes());

        if ( (message == null) || validator.validate(message.getAttributes()) != ValidationResult.success()) {
            return UStatus.newBuilder()
                .setCode(UCode.INVALID_ARGUMENT)
                .setMessage("Invalid message attributes")
                .build();
        }

        if (message.getAttributes().getType() == UMessageType.UMESSAGE_TYPE_REQUEST) {
            UMessage response = buildResponse(message);
            Executors.newSingleThreadExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    for (Iterator<UListener> it = listeners.iterator(); it.hasNext();) {
                        UListener listener = it.next();
                        listener.onReceive(response);
                    }
                }
            });
        }        

        return UStatus.newBuilder().setCode(UCode.OK).build();
    }

    /*
     * Register a listener based on the source and sink URIs. 
     */
    @Override
    public UStatus registerListener(UUri source, UUri sink, UListener listener) {
        listeners.add(listener);
        return UStatus.newBuilder().setCode(UCode.OK).build();
    }

    @Override
    public UStatus unregisterListener(UUri source, UUri sink, UListener listener) {
        final UStatus result = UStatus.newBuilder().setCode(listeners.contains(listener) ? 
            UCode.OK : UCode.INVALID_ARGUMENT).build();
        if (listeners.contains(listener)) {
            listeners.remove(listener);
        }
        return result;
    }

    @Override
    public UUri getSource() {
        return mSource;
    }

    @Override
    public void close() {
        listeners.clear();
    }
}


/**
 * Timeout uTransport simply does not send a reply
 */
class TimeoutUTransport extends TestUTransport {
    @Override
    public UStatus send(UMessage message) {
        return UStatus.newBuilder().setCode(UCode.OK).build();
    }
};

class ErrorUTransport extends TestUTransport {
    @Override
    public UStatus send(UMessage message) {
        return UStatus.newBuilder().setCode(UCode.FAILED_PRECONDITION).build();
    }

    @Override
    public UStatus registerListener(UUri source, UUri sink, UListener listener) {
        return UStatus.newBuilder().setCode(UCode.FAILED_PRECONDITION).build();
    }

    @Override
    public UStatus unregisterListener(UUri source, UUri sink, UListener listener) {
        return UStatus.newBuilder().setCode(UCode.FAILED_PRECONDITION).build();
    }
};

/**
 * Test UTransport that will set the commstatus for an error
 */
class CommStatusTransport extends TestUTransport {
    @Override
    public UMessage buildResponse(UMessage request) {
        UStatus status = UStatus.newBuilder()
            .setCode(UCode.FAILED_PRECONDITION)
            .setMessage("CommStatus Error")
            .build();
        return UMessageBuilder.response(request.getAttributes())
            .withCommStatus(status.getCode())
            .build(UPayload.pack(status));
    }
};


class EchoUTransport extends TestUTransport {
    @Override
    public UMessage buildResponse(UMessage request) {
        return request;
    }

    @Override
    public UStatus send(UMessage message) {
        UMessage response = buildResponse(message);
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                for (Iterator<UListener> it = listeners.iterator(); it.hasNext();) {
                    UListener listener = it.next();
                    listener.onReceive(response);
                }
            }
        });
        return UStatus.newBuilder().setCode(UCode.OK).build();
    }
};
