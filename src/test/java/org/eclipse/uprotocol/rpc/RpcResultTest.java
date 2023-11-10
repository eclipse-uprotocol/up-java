/*
 * Copyright (c) 2023 General Motors GTO LLC
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * SPDX-FileType: SOURCE
 * SPDX-FileCopyrightText: 2023 General Motors GTO LLC
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.uprotocol.rpc;

import com.google.rpc.Code;
import com.google.rpc.Status;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RpcResultTest {

    @Test
    public void test_isSuccess_on_Success() {
        RpcResult<Integer> result = RpcResult.success(2);
        assertTrue(result.isSuccess());
    }

    @Test
    public void test_isSuccess_on_Failure() {
        RpcResult<Integer> result = RpcResult.failure(Code.INVALID_ARGUMENT, "boom");
        assertFalse(result.isSuccess());
    }

    @Test
    public void test_isFailure_on_Success() {
        RpcResult<Integer> result = RpcResult.success(2);
        assertFalse(result.isFailure());
    }

    @Test
    public void test_isFailure_on_Failure() {
        RpcResult<Integer> result = RpcResult.failure(Code.INVALID_ARGUMENT, "boom");
        assertTrue(result.isFailure());
    }

    @Test
    public void testGetOrElseOnSuccess() {
        RpcResult<Integer> result = RpcResult.success(2);
        assertEquals(Integer.valueOf(2), result.getOrElse(this::getDefault));
    }

    @Test
    public void testGetOrElseOnFailure() {
        RpcResult<Integer> result = RpcResult.failure(Code.INVALID_ARGUMENT, "boom");
        assertEquals(getDefault(), result.getOrElse(this::getDefault));
    }

    @Test
    public void testGetOrElseOnSuccess_() {
        RpcResult<Integer> result = RpcResult.success(2);
        assertEquals(Integer.valueOf(2), result.getOrElse(5));
    }

    @Test
    public void testGetOrElseOnFailure_() {
        RpcResult<Integer> result = RpcResult.failure(Code.INVALID_ARGUMENT, "boom");
        assertEquals(Integer.valueOf(5), result.getOrElse(5));
    }

    @Test
    public void testSuccessValue_onSuccess() {
        RpcResult<Integer> result = RpcResult.success(2);
        assertEquals(Integer.valueOf(2), result.successValue());
    }

    @Test
    public void testSuccessValue_onFailure_() {
        RpcResult<Integer> result = RpcResult.failure(Code.INVALID_ARGUMENT, "boom");
        Exception exception =  assertThrows(IllegalStateException.class, result::successValue);
        assertEquals(exception.getMessage(), "Method successValue() called on a Failure instance");
    }

    @Test
    public void testFailureValue_onSuccess() {
        RpcResult<Integer> result = RpcResult.success(2);
        Exception exception =  assertThrows(IllegalStateException.class, result::failureValue);
        assertEquals(exception.getMessage(), "Method failureValue() called on a Success instance");
    }

    @Test
    public void testFailureValue_onFailure_() {
        RpcResult<Integer> result = RpcResult.failure(Code.INVALID_ARGUMENT, "boom");
        final Status resultValue = result.failureValue();
        assertEquals(Status.newBuilder()
                .setCode(Code.INVALID_ARGUMENT.getNumber())
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
    public void testMapSuccess_when_function_throws_exception() {
        RpcResult<Integer> result = RpcResult.success(2);
        final RpcResult<Integer> mapped = result.map(this::funThatThrowsAnExceptionForMap);
        assertTrue(mapped.isFailure());
        assertEquals(Code.UNKNOWN_VALUE, mapped.failureValue().getCode());
        assertEquals("2 went boom", mapped.failureValue().getMessage());
    }

    private int funThatThrowsAnExceptionForMap(int x) {
        throw new NullPointerException(String.format("%s went boom", x));
    }

    @Test
    public void testMapOnFailure() {
        RpcResult<Integer> result = RpcResult.failure(Code.INVALID_ARGUMENT, "boom");
        final RpcResult<Integer> mapped = result.map(x -> x * 2);
        assertTrue(mapped.isFailure());
        assertEquals(Status.newBuilder()
                .setCode(Code.INVALID_ARGUMENT.getNumber())
                .setMessage("boom").build(), mapped.failureValue());
    }

    @Test
    public void testFlatMapSuccess_when_function_throws_exception() {
        RpcResult<Integer> result = RpcResult.success(2);
        final RpcResult<Integer> flatMapped = result.flatMap(this::funThatThrowsAnExceptionForFlatMap);
        assertTrue(flatMapped.isFailure());
        assertEquals(Code.UNKNOWN_VALUE, flatMapped.failureValue().getCode());
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
        RpcResult<Integer> result = RpcResult.failure(Code.INVALID_ARGUMENT, "boom");
        final RpcResult<Integer> flatMapped = result.flatMap(x -> RpcResult.success(x * 2));
        assertTrue(flatMapped.isFailure());
        assertEquals(Status.newBuilder()
                .setCode(Code.INVALID_ARGUMENT.getNumber())
                .setMessage("boom").build(), flatMapped.failureValue());
    }

    @Test
    public void testFilterOnSuccess_that_fails() {
        RpcResult<Integer> result = RpcResult.success(2);
        final RpcResult<Integer> filterResult = result.filter(i -> i > 5);
        assertTrue(filterResult.isFailure());
        assertEquals(Status.newBuilder()
                .setCode(Code.FAILED_PRECONDITION.getNumber())
                .setMessage("filtered out").build(), filterResult.failureValue());
    }

    @Test
    public void testFilterOnSuccess_that_succeeds() {
        RpcResult<Integer> result = RpcResult.success(2);
        final RpcResult<Integer> filterResult = result.filter(i -> i < 5);
        assertTrue(filterResult.isSuccess());
        assertEquals(2, filterResult.successValue());
    }

    @Test
    public void testFilterOnSuccess__when_function_throws_exception() {
        RpcResult<Integer> result = RpcResult.success(2);
        final RpcResult<Integer> filterResult = result.filter(this::predicateThatThrowsAnException);
        assertTrue(filterResult.isFailure());
        assertEquals(Status.newBuilder()
                .setCode(Code.UNKNOWN_VALUE)
                .setMessage("2 went boom").build(), filterResult.failureValue());
    }

    private boolean predicateThatThrowsAnException(int x) {
        throw new NullPointerException(String.format("%s went boom", x));
    }

    @Test
    public void testFilterOnFailure() {
        RpcResult<Integer> result = RpcResult.failure(Code.INVALID_ARGUMENT, "boom");
        final RpcResult<Integer> filterResult = result.filter(i -> i > 5);
        assertTrue(filterResult.isFailure());
        assertEquals(Status.newBuilder()
                .setCode(Code.INVALID_ARGUMENT.getNumber())
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
    public void testFlattenOnSuccess_with_function_that_fails() {
        RpcResult<Integer> result = RpcResult.success(2);
        final RpcResult<RpcResult<Integer>> mapped = result.map(this::funThatThrowsAnExceptionForFlatMap);
        final RpcResult<Integer> mappedFlattened = RpcResult.flatten(mapped);
        assertTrue(mappedFlattened.isFailure());
        assertEquals(Code.UNKNOWN_VALUE, mappedFlattened.failureValue().getCode());
        assertEquals("2 went boom", mappedFlattened.failureValue().getMessage());
    }

    @Test
    public void testFlattenOnFailure() {
        RpcResult<Integer> result = RpcResult.failure(Code.INVALID_ARGUMENT, "boom");
        final RpcResult<RpcResult<Integer>> mapped = result.map(this::multiplyBy2);
        final RpcResult<Integer> mappedFlattened = RpcResult.flatten(mapped);
        assertTrue(mappedFlattened.isFailure());
        assertEquals(Status.newBuilder()
                .setCode(Code.INVALID_ARGUMENT.getNumber())
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
        RpcResult<Integer> result = RpcResult.failure(Code.INVALID_ARGUMENT, "boom");
        assertEquals("Failure(code: 3\n" +
                "message: \"boom\"\n" +
                ")", result.toString());
    }

}