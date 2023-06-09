/*
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
 */
package org.eclipse.uprotocol.status.factory;

import java.io.InvalidClassException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeoutException;

import org.eclipse.uprotocol.status.datamodel.UStatus;

import com.google.protobuf.Any;
import com.google.rpc.Code;


/**
 * Factory for Building uProtocol Status messages
 */
public interface UStatusFactory {


    /**
     * Build Any kind of status passing all the variable types 
     * 
     * @param code google.rpc.Code for the Status
     * @param message Status Message
     * @param details Any additional details for the Status
     * @return uStatus object
     */
    static UStatus buildUStatus(Code code, String message, Any details) {
        return new UStatus(code, message, details);
    }

    
   /**
     * Build Any kind of status passing only the Code 
     * 
     * @param code google.rpc.Code for the Status
     * @return uStatus object
     */
    static UStatus buildUStatus(Code code) {
        return new UStatus(code);
    }


    /**
     * Build OK Status
     * 
     * @param code google.rpc.Code for the Status
     * @return uStatus object
     */
    static UStatus buildOkUStatus() {
        return new UStatus(Code.OK);
    }


    /**
     * Convert an exception to a UStatus
     * 
     * @param exception The exception that we want to convert
     * @return uStatus object
     */
    static UStatus throwableToUStatus(Throwable exception) {
        if (exception instanceof SecurityException) {
            return buildUStatus(Code.PERMISSION_DENIED);
        } else if (exception instanceof InvalidClassException) {
            return buildUStatus(Code.INVALID_ARGUMENT);
        } else if (exception instanceof IllegalArgumentException) {
            return buildUStatus(Code.INVALID_ARGUMENT);
        } else if (exception instanceof NullPointerException) {
            return buildUStatus(Code.INVALID_ARGUMENT);
        } else if (exception instanceof CancellationException) {
            return buildUStatus(Code.CANCELLED);
        } else if (exception instanceof IllegalStateException) {
            return buildUStatus(Code.UNAVAILABLE);
        } else if (exception instanceof UnsupportedOperationException) {
            return buildUStatus(Code.UNIMPLEMENTED);
        } else if (exception instanceof InterruptedException) {
            return buildUStatus(Code.CANCELLED);
        } else if (exception instanceof TimeoutException) {
            return buildUStatus(Code.DEADLINE_EXCEEDED);
        } else {
            return buildUStatus(Code.UNKNOWN);
        }
    }

}
