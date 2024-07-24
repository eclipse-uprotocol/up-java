package org.eclipse.uprotocol.communication;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CompletableFuture;

import org.eclipse.uprotocol.core.usubscription.v3.SubscriptionRequest;
import org.eclipse.uprotocol.core.usubscription.v3.SubscriptionResponse;
import org.eclipse.uprotocol.core.usubscription.v3.SubscriptionStatus;
import org.eclipse.uprotocol.core.usubscription.v3.UnsubscribeResponse;
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

import com.google.protobuf.InvalidProtocolBufferException;

/**
 * TestUTransport is a test implementation of the UTransport interface 
 * that can only hold a single listener for testing.
 */
public class TestUTransport implements UTransport {

    protected List<UListener> listeners = new CopyOnWriteArrayList<>();
    private final UUri mSource;

    public UMessage buildResponse(UMessage request) {
        // If the request is a subscribe or unsubscribe request, return the appropriate response
        if (request.getAttributes().getSink().getUeId() == 0) {
            if (request.getAttributes().getSink().getResourceId() == 1) {
                try {
                    SubscriptionRequest subscriptionRequest = SubscriptionRequest.parseFrom(request.getPayload());
                    SubscriptionResponse subResponse = SubscriptionResponse.newBuilder()
                        .setTopic(subscriptionRequest.getTopic())
                        .setStatus(SubscriptionStatus.newBuilder()
                            .setState(SubscriptionStatus.State.SUBSCRIBED).build())
                        .build();
                    return UMessageBuilder.response(request.getAttributes()).build(UPayload.pack(subResponse));
                } catch (InvalidProtocolBufferException e) {
                    return UMessageBuilder.response(request.getAttributes()).build(
                        UPayload.pack(UnsubscribeResponse.newBuilder().build()));
                }
            } else {
                return UMessageBuilder.response(request.getAttributes()).build(
                    UPayload.pack(UnsubscribeResponse.newBuilder().build()));
            }
        }
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
    public CompletionStage<UStatus> send(UMessage message) {
        UAttributesValidator validator = UAttributesValidator.getValidator(message.getAttributes());

        if ( (message == null) || validator.validate(message.getAttributes()) != ValidationResult.success()) {
            return CompletableFuture.completedFuture(UStatus.newBuilder()
                .setCode(UCode.INVALID_ARGUMENT)
                .setMessage("Invalid message attributes")
                .build());
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

        return CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.OK).build());
    }

    /*
     * Register a listener based on the source and sink URIs. 
     */
    @Override
    public CompletionStage<UStatus> registerListener(UUri source, UUri sink, UListener listener) {
        listeners.add(listener);
        return CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.OK).build());
    }

    @Override
    public CompletionStage<UStatus> unregisterListener(UUri source, UUri sink, UListener listener) {
        final UStatus result = UStatus.newBuilder().setCode(listeners.contains(listener) ? 
            UCode.OK : UCode.NOT_FOUND).build();
        if (listeners.contains(listener)) {
            listeners.remove(listener);
        }
        return CompletableFuture.completedFuture(result);
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
    public CompletionStage<UStatus> send(UMessage message) {
        return CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.OK).build());
    }
};

class ErrorUTransport extends TestUTransport {
    @Override
    public CompletionStage<UStatus> send(UMessage message) {
        return CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.FAILED_PRECONDITION).build());
    }

    @Override
    public CompletionStage<UStatus> registerListener(UUri source, UUri sink, UListener listener) {
        return CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.FAILED_PRECONDITION).build());
    }

    @Override
    public CompletionStage<UStatus> unregisterListener(UUri source, UUri sink, UListener listener) {
        return CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.FAILED_PRECONDITION).build());
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

/**
 * Test UTransport that will set the commstatus for a success response
 */
class CommStatusOkTransport extends TestUTransport {
    @Override
    public UMessage buildResponse(UMessage request) {
        UStatus status = UStatus.newBuilder()
                .setCode(UCode.OK)
                .setMessage("No Communication Error")
                .build();
        return UMessageBuilder.response(request.getAttributes())
                .withCommStatus(status.getCode())
                .build(UPayload.pack(status));
    }
}

class EchoUTransport extends TestUTransport {
    @Override
    public UMessage buildResponse(UMessage request) {
        return request;
    }

    @Override
    public CompletionStage<UStatus> send(UMessage message) {
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
        return CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.OK).build());
    }
};
