package org.eclipse.uprotocol.communication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.eclipse.uprotocol.v1.UCode;
import org.eclipse.uprotocol.v1.UStatus;
import org.eclipse.uprotocol.v1.UUri;


public class RpcMapperTest {
 
    @Test
    @DisplayName("Test RpcMapper mapResponse using RpcClient interface invokeMethod API")
    public void testMapResponse() {
        UUri uri = UUri.newBuilder().setAuthorityName("Hartley)").build();
        UPayload payload = UPayload.pack(uri);

        RpcClient rpcClient = new InMemoryRpcClient(new TestUTransport());
        final CompletionStage<UUri> result = RpcMapper.mapResponse(
            rpcClient.invokeMethod(createMethodUri(), payload, null), UUri.class);
        UUri uri1 = result.toCompletableFuture().join();
        assertFalse(result.toCompletableFuture().isCompletedExceptionally());
        assertEquals(uri, uri1);
    }


    @Test
    @DisplayName("Test RpcMapper mapResponseToResult using HappyPathUTransport when the request is empty")
    public void test_map_response_to_result_with_empty_request() {
        RpcClient rpcClient = new InMemoryRpcClient(new TestUTransport());
        final RpcResult<UUri> result = RpcMapper.mapResponseToResult(
            rpcClient.invokeMethod(createMethodUri(), null, null), 
                UUri.class).toCompletableFuture().join();
        assertTrue(result.isSuccess());
        assertEquals(result.successValue(), UUri.getDefaultInstance());
    }

    @Test
    @DisplayName("Test RpcMapper mapResponse when the future is completed exceptionally")
    public void test_map_response_with_exception() {
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
    public void test_map_response_with_empty_payload() {
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
    public void test_map_response_with_null_payload() {
        RpcClient rpcClient = new RpcClient() {
            @Override
            public CompletionStage<UPayload> invokeMethod(UUri uri, UPayload payload, CallOptions options) {
                return CompletableFuture.completedFuture(null);
            }
        };
        final CompletionStage<UUri> result = RpcMapper.mapResponse(
            rpcClient.invokeMethod(createMethodUri(), UPayload.EMPTY, null), UUri.class);
 
        Exception exception = assertThrows(ExecutionException.class,result.toCompletableFuture()::get);
        assertEquals(exception.getMessage(), 
            String.format("java.lang.RuntimeException: Unknown payload. Expected [%s]", UUri.class.getName()));
    }
    

    @Test
    @DisplayName("Test RpcMapper mapResponseToResult when the returned payload is not empty")
    public void test_map_response_to_result_with_non_empty_payload() {
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
    public void test_map_response_to_result_with_null_payload() {
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
    public void test_map_response_to_result_with_empty_payload() {
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
    public void test_map_response_to_result_with_exception() {
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
    public void test_map_response_to_result_with_timeout_exception() {
        RpcClient rpcClient = new RpcClient() {
            @Override
            public CompletionStage<UPayload> invokeMethod(UUri uri, UPayload payload, CallOptions options) {
                return CompletableFuture.failedFuture(new TimeoutException());
            }
        };
        final RpcResult<UUri> result = RpcMapper.mapResponseToResult(
            rpcClient.invokeMethod(createMethodUri(), UPayload.EMPTY, null), UUri.class).toCompletableFuture().join();
        assertTrue(result.isFailure());
        assertEquals(result.failureValue().getCode(), UCode.DEADLINE_EXCEEDED);
        assertEquals(result.failureValue().getMessage(), "Request timed out");
    }

    @Test
    @DisplayName("Test RpcMapper mapResponseToResult when completed with invalid arguments exception")
    public void test_map_response_to_result_with_invalid_arguments_exception() {
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
