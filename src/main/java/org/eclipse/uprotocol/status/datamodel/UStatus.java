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

 /**
  * uProtocol Status that wraps google.rpc.Status
  */
package org.eclipse.uprotocol.status.datamodel;

import java.util.Objects;

import com.google.protobuf.Any;
import com.google.rpc.Code;
import com.google.rpc.Status;

public class UStatus {
    
    private Status status; 
    private static final UStatus EMPTY = new UStatus();
    

    public UStatus() {
        this.status = Status.getDefaultInstance();
    }


    public UStatus(Status status) {
        this.status = Objects.requireNonNullElse(status, Status.getDefaultInstance());
    }


    public UStatus(Code code) {
      this.status = Objects.requireNonNullElse(Status.newBuilder()
          .setCode(code.getNumber())
          .build(),
          Status.getDefaultInstance());
    }


    public UStatus(Code code, String message, Any details) {
      this.status = Objects.requireNonNullElse(Status.newBuilder()
          .setCode(code.getNumber())
          .setMessage(message)
          .addDetails(details)
          .build(),
          Status.getDefaultInstance());
    }


    public Status getStatus() {
        return status;
    }


    public static UStatus empty() {
        return EMPTY;
    }


    public boolean isEmpty() {
      return (this.status == Status.getDefaultInstance());
    }
}
