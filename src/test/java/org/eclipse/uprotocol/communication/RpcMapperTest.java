package org.eclipse.uprotocol.communication;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.eclipse.uprotocol.v1.UCode;
import org.eclipse.uprotocol.v1.UStatus;
import org.eclipse.uprotocol.v1.UUri;


public class RpcMapperTest {
 
    @Test
    @DisplayName("Test RpcMapper mapResponse using RpcClient interface invokeMethod API")
    public void testMapResponse() {
        UPayload payload = UPayload.packToAny(UUri.newBuilder().build());
        RpcClient rpcClient = new RpcClient() {
            @Override
            public CompletionStage<UPayload> invokeMethod(UUri uri, UPayload payload, CallOptions options) {
                return CompletableFuture.completedFuture(payload);
            }
        };
        final CompletionStage<UPayload> response = rpcClient.invokeMethod(createMethodUri(), payload, null);
        assertNotNull(response);
        assertDoesNotThrow(() -> {
            UPayload payload1 = response.toCompletableFuture().get();
            assertTrue(payload.equals(payload1));
        });
    }


    @Test
    @DisplayName("Test RpcMapper mapResponseToResult using HappyPathUTransport when the request is empty")
    public void testMapResponseToResultWithEmptyRequest() {
        RpcClient rpcClient = new RpcClient() {
            @Override
            public CompletionStage<UPayload> invokeMethod(UUri uri, UPayload payload, CallOptions options) {
                return CompletableFuture.completedFuture(UPayload.EMPTY);
            }
        };
        final RpcResult<UUri> result = RpcMapper.mapResponseToResult(
            rpcClient.invokeMethod(createMethodUri(), null, null), 
                UUri.class).toCompletableFuture().join();
        assertTrue(result.isSuccess());
        assertEquals(result.successValue(), UUri.getDefaultInstance());
    }

    @Test
    @DisplayName("Test RpcMapper mapResponse when the future is completed exceptionally")
    public void testMapResponseWithException() {
        RpcClient rpcClient = new RpcClient() {
            @Override
            public CompletionStage<UPayload> invokeMethod(UUri uri, UPayload payload, CallOptions options) {
                return CompletableFuture.failedFuture(new RuntimeException("Error"));
            }
        };

        final CompletionStage<UUri> result = RpcMapper.mapResponse(
            rpcClient.invokeMethod(createMethodUri(), null, null), UUri.class);
        assertTrue(result.toCompletableFuture().isCompletedExceptionally());
    }


    @Test
    @DisplayName("Test RpcMapper mapResponse when the returned payload is empty")
    public void testMapResponseWithEmptyPayload() {
        RpcClient rpcClient = new RpcClient() {
            @Override
            public CompletionStage<UPayload> invokeMethod(UUri uri, UPayload payload, CallOptions options) {
                return CompletableFuture.completedFuture(UPayload.EMPTY);
            }
        };
        final CompletionStage<UUri> result = RpcMapper.mapResponse(
            rpcClient.invokeMethod(createMethodUri(), UPayload.EMPTY, null), UUri.class);
        assertFalse(result.toCompletableFuture().isCompletedExceptionally());
        assertEquals(result.toCompletableFuture().join(), UUri.getDefaultInstance());
    }

    @Test
    @DisplayName("Test RpcMapper mapResponse when the returned payload is null")
    public void testMapResponseWithNullPayload() {
        RpcClient rpcClient = new RpcClient() {
            @Override
            public CompletionStage<UPayload> invokeMethod(UUri uri, UPayload payload, CallOptions options) {
                return CompletableFuture.completedFuture(null);
            }
        };
        final CompletionStage<UUri> result = RpcMapper.mapResponse(
            rpcClient.invokeMethod(createMethodUri(), UPayload.EMPTY, null), UUri.class);
 
        Exception exception = assertThrows(ExecutionException.class, result.toCompletableFuture()::get);
        assertEquals(exception.getMessage(), 
            String.format("java.lang.RuntimeException: Unknown payload. Expected [%s]", UUri.class.getName()));
    }
    

    @Test
    @DisplayName("Test RpcMapper mapResponseToResult when the returned payload is not empty")
    public void testMapResponseToResultWithNonEmptyPayload() {
        UUri uri = UUri.newBuilder().setAuthorityName("Hartley").build();
        UPayload payload = UPayload.pack(uri);
        RpcClient rpcClient = new RpcClient() {
            @Override
            public CompletionStage<UPayload> invokeMethod(UUri uri, UPayload payload, CallOptions options) {
                return CompletableFuture.completedFuture(payload);
            }
        };
        final RpcResult<UUri> result = RpcMapper.mapResponseToResult(
            rpcClient.invokeMethod(createMethodUri(), payload, null), UUri.class).toCompletableFuture().join();
        assertTrue(result.isSuccess());
        assertEquals(result.successValue(), uri);
    }

    @Test
    @DisplayName("Test RpcMapper mapResponseToResult when the returned payload is null")
    public void testMapResponseToResultWithNullPayload() {
        RpcClient rpcClient = new RpcClient() {
            @Override
            public CompletionStage<UPayload> invokeMethod(UUri uri, UPayload payload, CallOptions options) {
                return CompletableFuture.completedFuture(null);
            }
        };
        final RpcResult<UUri> result = RpcMapper.mapResponseToResult(
            rpcClient.invokeMethod(createMethodUri(), UPayload.EMPTY, null), UUri.class).toCompletableFuture().join();
        assertTrue(result.isFailure());
    }

    @Test
    @DisplayName("Test RpcMapper mapResponseToResult when the returned payload is empty")
    public void testMapResponseToResultWithEmptyPayload() {
        RpcClient rpcClient = new RpcClient() {
            @Override
            public CompletionStage<UPayload> invokeMethod(UUri uri, UPayload payload, CallOptions options) {
                return CompletableFuture.completedFuture(UPayload.EMPTY);
            }
        };
        final RpcResult<UUri> result = RpcMapper.mapResponseToResult(
            rpcClient.invokeMethod(createMethodUri(), UPayload.EMPTY, null), UUri.class).toCompletableFuture().join();
        assertTrue(result.isSuccess());
        assertEquals(result.successValue(), UUri.getDefaultInstance());
    }

    @Test
    @DisplayName("Test RpcMapper mapResponseToResult when the future is completed exceptionally")
    public void testMapResponseToResultWithException() {
        RpcClient rpcClient = new RpcClient() {
            @Override
            public CompletionStage<UPayload> invokeMethod(UUri uri, UPayload payload, CallOptions options) {
                UStatus status = UStatus.newBuilder().setCode(UCode.FAILED_PRECONDITION).setMessage("Error").build();
                return CompletableFuture.failedFuture(new UStatusException(status));
            }
        };
        final RpcResult<UUri> result = RpcMapper.mapResponseToResult(
            rpcClient.invokeMethod(createMethodUri(), UPayload.EMPTY, null), UUri.class).toCompletableFuture().join();
        assertTrue(result.isFailure());
        assertEquals(result.failureValue().getCode(), UCode.FAILED_PRECONDITION);
        assertEquals(result.failureValue().getMessage(), "Error");
    }

    @Test
    @DisplayName("Test RpcMapper mapResponseToResult when completed exceptionally with a timeout exception")
    public void testMapResponseToResultWithTimeoutException() {
        RpcClient rpcClient = new RpcClient() {
            @Override
            public CompletionStage<UPayload> invokeMethod(UUri uri, UPayload payload, CallOptions options) {
                return CompletableFuture.failedFuture(
                    new UStatusException(UCode.DEADLINE_EXCEEDED, "Request timed out"));
            }
        };
        final CompletionStage<RpcResult<UUri>> result = RpcMapper.mapResponseToResult(
            rpcClient.invokeMethod(createMethodUri(), UPayload.EMPTY, null), UUri.class);
        
        assertDoesNotThrow(() -> {
            RpcResult<UUri> result1 = result.toCompletableFuture().get();
            assertEquals(result1.failureValue().getCode(), UCode.DEADLINE_EXCEEDED);
            assertEquals(result1.failureValue().getMessage(), "Request timed out");
        });
    }

    @Test
    @DisplayName("Test RpcMapper mapResponseToResult when completed with invalid arguments exception")
    public void testMapResponseToResultWithInvalidArgumentsException() {
        RpcClient rpcClient = new RpcClient() {
            @Override
            public CompletionStage<UPayload> invokeMethod(UUri uri, UPayload payload, CallOptions options) {
                return CompletableFuture.failedFuture(new IllegalArgumentException());
            }
        };
        final RpcResult<UUri> result = RpcMapper.mapResponseToResult(
            rpcClient.invokeMethod(createMethodUri(), UPayload.EMPTY, null), UUri.class).toCompletableFuture().join();
        assertTrue(result.isFailure());
        assertEquals(result.failureValue().getCode(), UCode.INVALID_ARGUMENT);
        assertEquals(result.failureValue().getMessage(), "No message provided");
    }


    private UUri createMethodUri() {
        return UUri.newBuilder()
            .setAuthorityName("hartley")
            .setUeId(10)
            .setUeVersionMajor(1)
            .setResourceId(3).build();
    }
}
