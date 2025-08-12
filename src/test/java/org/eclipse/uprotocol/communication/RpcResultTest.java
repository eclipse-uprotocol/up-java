/**
 * SPDX-FileCopyrightText: 2024 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.eclipse.uprotocol.communication;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.uprotocol.v1.UCode;
import org.eclipse.uprotocol.v1.UStatus;

class RpcResultTest {

    @Test
    public void testIsSuccessOnSuccess() {
        RpcResult<Integer> result = RpcResult.success(2);
        assertTrue(result.isSuccess());
    }

    @Test
    public void testIsSuccessOnFailure() {
        RpcResult<Integer> result = RpcResult.failure(UCode.INVALID_ARGUMENT, "boom");
        assertFalse(result.isSuccess());
    }

    @Test
    public void testIsFailureOnSuccess() {
        RpcResult<Integer> result = RpcResult.success(2);
        assertFalse(result.isFailure());
    }

    @Test
    public void testIsFailureOnFailure() {
        RpcResult<Integer> result = RpcResult.failure(UCode.INVALID_ARGUMENT, "boom");
        assertTrue(result.isFailure());
    }

    @Test
    public void testGetOrElseOnSuccess() {
        RpcResult<Integer> result = RpcResult.success(2);
        assertEquals(Integer.valueOf(2), result.getOrElse(this::getDefault));
    }

    @Test
    public void testGetOrElseOnFailure() {
        RpcResult<Integer> result = RpcResult.failure(UCode.INVALID_ARGUMENT, "boom");
        assertEquals(getDefault(), result.getOrElse(this::getDefault));
    }

    @Test
    public void testGetOrElseOnSuccessWithValue() {
        RpcResult<Integer> result = RpcResult.success(2);
        assertEquals(Integer.valueOf(2), result.getOrElse(5));
    }

    @Test
    public void testGetOrElseOnFailureWithValue() {
        RpcResult<Integer> result = RpcResult.failure(UCode.INVALID_ARGUMENT, "boom");
        assertEquals(Integer.valueOf(5), result.getOrElse(5));
    }

    @Test
    public void testSuccessValueOnSuccess() {
        RpcResult<Integer> result = RpcResult.success(2);
        assertEquals(Integer.valueOf(2), result.successValue());
    }

    @Test
    public void testSuccessValueOnFailure() {
        RpcResult<Integer> result = RpcResult.failure(UCode.INVALID_ARGUMENT, "boom");
        Exception exception =  assertThrows(IllegalStateException.class, result::successValue);
        assertEquals(exception.getMessage(), "Method successValue() called on a Failure instance");
    }

    @Test
    public void testFailureValueOnSuccess() {
        RpcResult<Integer> result = RpcResult.success(2);
        Exception exception =  assertThrows(IllegalStateException.class, result::failureValue);
        assertEquals(exception.getMessage(), "Method failureValue() called on a Success instance");
    }

    @Test
    public void testFailureValueOnFailure() {
        RpcResult<Integer> result = RpcResult.failure(UCode.INVALID_ARGUMENT, "boom");
        final UStatus resultValue = result.failureValue();
        assertEquals(UStatus.newBuilder()
                .setCode(UCode.INVALID_ARGUMENT)
                .setMessage("boom").build(), resultValue);
    }

    private int getDefault() {
        return 5;
    }

    @Test
    public void testMapOnSuccess() {
        RpcResult<Integer> result = RpcResult.success(2);
        final RpcResult<Integer> mapped = result.map(x -> x * 2);
        assertTrue(mapped.isSuccess());
        assertEquals(4, mapped.successValue());
    }

    @Test
    public void testMapSuccessWhenFunctionThrowsException() {
        RpcResult<Integer> result = RpcResult.success(2);
        final RpcResult<Integer> mapped = result.map(this::funThatThrowsAnExceptionForMap);
        assertTrue(mapped.isFailure());
        assertEquals(UCode.UNKNOWN, mapped.failureValue().getCode());
        assertEquals("2 went boom", mapped.failureValue().getMessage());
    }

    private int funThatThrowsAnExceptionForMap(int x) {
        throw new NullPointerException(String.format("%s went boom", x));
    }

    @Test
    public void testMapOnFailure() {
        RpcResult<Integer> result = RpcResult.failure(UCode.INVALID_ARGUMENT, "boom");
        final RpcResult<Integer> mapped = result.map(x -> x * 2);
        assertTrue(mapped.isFailure());
        assertEquals(UStatus.newBuilder()
                .setCode(UCode.INVALID_ARGUMENT)
                .setMessage("boom").build(), mapped.failureValue());
    }

    @Test
    public void testFlatMapSuccessWhenFunctionThrowsException() {
        RpcResult<Integer> result = RpcResult.success(2);
        final RpcResult<Integer> flatMapped = result.flatMap(this::funThatThrowsAnExceptionForFlatMap);
        assertTrue(flatMapped.isFailure());
        assertEquals(UCode.UNKNOWN, flatMapped.failureValue().getCode());
        assertEquals("2 went boom", flatMapped.failureValue().getMessage());
    }

    private RpcResult<Integer> funThatThrowsAnExceptionForFlatMap(int x) {
        throw new NullPointerException(String.format("%s went boom", x));
    }


    @Test
    public void testFlatMapOnSuccess() {
        RpcResult<Integer> result = RpcResult.success(2);
        final RpcResult<Integer> flatMapped = result.flatMap(x -> RpcResult.success(x * 2));
        assertTrue(flatMapped.isSuccess());
        assertEquals(4, flatMapped.successValue());
    }

    @Test
    public void testFlatMapOnFailure() {
        RpcResult<Integer> result = RpcResult.failure(UCode.INVALID_ARGUMENT, "boom");
        final RpcResult<Integer> flatMapped = result.flatMap(x -> RpcResult.success(x * 2));
        assertTrue(flatMapped.isFailure());
        assertEquals(UStatus.newBuilder()
                .setCode(UCode.INVALID_ARGUMENT)
                .setMessage("boom").build(), flatMapped.failureValue());
    }

    @Test
    public void testFilterOnSuccessThatFails() {
        RpcResult<Integer> result = RpcResult.success(2);
        final RpcResult<Integer> filterResult = result.filter(i -> i > 5);
        assertTrue(filterResult.isFailure());
        assertEquals(UStatus.newBuilder()
                .setCode(UCode.FAILED_PRECONDITION)
                .setMessage("filtered out").build(), filterResult.failureValue());
    }

    @Test
    public void testFilterOnSuccessThatSucceeds() {
        RpcResult<Integer> result = RpcResult.success(2);
        final RpcResult<Integer> filterResult = result.filter(i -> i < 5);
        assertTrue(filterResult.isSuccess());
        assertEquals(2, filterResult.successValue());
    }

    @Test
    public void testFilterOnSuccessWhenFunctionThrowsException() {
        RpcResult<Integer> result = RpcResult.success(2);
        final RpcResult<Integer> filterResult = result.filter(this::predicateThatThrowsAnException);
        assertTrue(filterResult.isFailure());
        assertEquals(UStatus.newBuilder()
                .setCode(UCode.UNKNOWN)
                .setMessage("2 went boom").build(), filterResult.failureValue());
    }

    private boolean predicateThatThrowsAnException(int x) {
        throw new NullPointerException(String.format("%s went boom", x));
    }

    @Test
    public void testFilterOnFailure() {
        RpcResult<Integer> result = RpcResult.failure(UCode.INVALID_ARGUMENT, "boom");
        final RpcResult<Integer> filterResult = result.filter(i -> i > 5);
        assertTrue(filterResult.isFailure());
        assertEquals(UStatus.newBuilder()
                .setCode(UCode.INVALID_ARGUMENT)
                .setMessage("boom").build(), filterResult.failureValue());
    }

    @Test
    public void testFlattenOnSuccess() {
        RpcResult<Integer> result = RpcResult.success(2);
        final RpcResult<RpcResult<Integer>> mapped = result.map(this::multiplyBy2);
        final RpcResult<Integer> mappedFlattened = RpcResult.flatten(mapped);
        assertTrue(mappedFlattened.isSuccess());
        assertEquals(4, mappedFlattened.successValue());
    }

    @Test
    public void testFlattenOnSuccessWithFunctionThatFails() {
        RpcResult<Integer> result = RpcResult.success(2);
        final RpcResult<RpcResult<Integer>> mapped = result.map(this::funThatThrowsAnExceptionForFlatMap);
        final RpcResult<Integer> mappedFlattened = RpcResult.flatten(mapped);
        assertTrue(mappedFlattened.isFailure());
        assertEquals(UCode.UNKNOWN, mappedFlattened.failureValue().getCode());
        assertEquals("2 went boom", mappedFlattened.failureValue().getMessage());
    }

    @Test
    public void testFlattenOnFailure() {
        RpcResult<Integer> result = RpcResult.failure(UCode.INVALID_ARGUMENT, "boom");
        final RpcResult<RpcResult<Integer>> mapped = result.map(this::multiplyBy2);
        final RpcResult<Integer> mappedFlattened = RpcResult.flatten(mapped);
        assertTrue(mappedFlattened.isFailure());
        assertEquals(UStatus.newBuilder()
                .setCode(UCode.INVALID_ARGUMENT)
                .setMessage("boom").build(), mappedFlattened.failureValue());
    }

    private RpcResult<Integer> multiplyBy2(int x) {
        return RpcResult.success(x * 2);
    }

    @Test
    public void testToStringSuccess() {
        RpcResult<Integer> result = RpcResult.success(2);
        assertEquals("Success(2)", result.toString());
    }

    @Test
    public void testToStringFailure() {
        RpcResult<Integer> result = RpcResult.failure(UCode.INVALID_ARGUMENT, "boom");
        assertEquals("Failure(code: INVALID_ARGUMENT\n" +
                "message: \"boom\"\n" +
                ")", result.toString());
    }

}