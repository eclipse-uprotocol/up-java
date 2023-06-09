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
package org.eclipse.uprotocol.cloudevent.datamodel;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;


import org.eclipse.uprotocol.uri.datamodel.UUri;
import org.eclipse.uprotocol.uuid.factory.UUIDFactory;


/**
 * Micro CloudEvent is representation of a CloudEvent for constrained environments
 * optimized to hold fixed length messages.
 * 
 */
public class MicroCloudEvent {

    private final UUri sink;
    private final UUri source;
    private final Type type; 
    private final UUID  id = UUIDFactory.Factories.UPROTOCOL.factory().create();
    private final Priority priority;
    private final SpecVersion specversion = SpecVersion.ONE;
    private final Integer ttl;
    private final String dataschema;
    private final String datacontenttype;
    private final byte[] data;
    private final UUID reqId;

    /**
     * CloudEvent Spec version that we are compliant to.
     */
    public enum SpecVersion {
        ONE("1.0", 1);

        private final String string;
        private final Integer version;

        private static final Map<String, Type> BY_STRING = new HashMap<>();
        private static final Map<Integer, Type> BY_NUMBER = new HashMap<>();

        private SpecVersion(String string, int version) {
            this.version = version;
            this.string = string;
        }

        public Integer getVersion() {
            return version;
        }
        public String getString() {
            return string;
        }
        
        public static Type valueOfString(String string) {
            return BY_STRING.get(string);
        }
    
        public static Type valueOfNumber(int number) {
            return BY_NUMBER.get(number);
        }
    }


    /**
     * uProtocol CloudEvent Types
     */
    public enum Type {
        PUBLISH ("pub.v1", 0),
        NOTIFICATION("notif.v1", 1),
        REQUEST("req.v1", 2),
        RESPONSE("res.v1", 3);


        private static final Map<String, Type> BY_STRING = new HashMap<>();
        private static final Map<Integer, Type> BY_NUMBER = new HashMap<>();
        
        static {
            for (Type e : values()) {
                BY_STRING.put(e.string, e);
                BY_NUMBER.put(e.number, e);
            }
        }
    
        public final String string;
        public final int number;
    
        private Type(String string, int number) {
            this.string = string;
            this.number = number;
        }
    
        public static Type valueOfString(String string) {
            return BY_STRING.get(string);
        }
    
        public static Type valueOfNumber(int number) {
            return BY_NUMBER.get(number);
        }
    
    }

    
    /**
     * uProtocol CloudEvent priority
     */
    public enum Priority {
        // Low Priority. No bandwidth assurance such as File Transfer.
        LOW ("CS0", 0),
        // Standard, undifferentiated application such as General (unclassified).
        STANDARD ("CS1", 1),
        // Operations, Administration, and Management such as Streamer messages (sub, connect, etc…)
        OPERATIONS ("CS2", 2),
        // Multimedia streaming such as Video Streaming
        MULTIMEDIA_STREAMING ("CS3", 3),
        // Real-time interactive such as High priority (rpc events)
        REALTIME_INTERACTIVE ("CS4", 4),
        // Signaling such as Important
        SIGNALING("CS5", 5),
        // Network control such as Safety Critical
        NETWORK_CONTROL ("CS6", 6);

        private final String string;
        private final Integer number;
        
        private static final Map<String, Priority> BY_STRING = new HashMap<>();
        private static final Map<Integer, Priority> BY_NUMBER = new HashMap<>();
        
        static {
            for (Priority e : values()) {
                BY_STRING.put(e.string, e);
                BY_NUMBER.put(e.number, e);
            }
        }

        public String getString() {
            return string;
        }

        public Integer getNumber() {
            return number;
        }

        Priority(String string, Integer number) {
            this.string = string;
            this.number = number;
        }
        

        public static Optional<Priority> get(String val) {
            return Arrays.stream(Priority.values())
                .filter(priority -> priority.string.equals(val))
                .findFirst();
        }
    }


    private MicroCloudEvent(MicroCloudEventBuilder builder) {
        this.type = builder.type;
        this.sink = builder.sink;
        this.source = builder.source;
        this.ttl = builder.ttl;
        this.priority = builder.priority;
        this.data = builder.data;
        this.datacontenttype = builder.datacontenttype;
        this.dataschema = builder.dataschema;
        this.reqId = builder.reqId;
    }

    public UUri getSink() {
        return sink;
    }
    
    public UUri getSource() {
        return source;
    }

    public Type getType() {
        return type;
    }

    public UUID getId() {
        return id;
    }

    public Priority getPriority() {
        return priority;
    }

    public SpecVersion getSpecVersion() {
        return specversion;
    }

    public Integer getTtl() {
        return ttl;
    }

    public String getDataSchema() {
        return dataschema;
    }

    public String getDataContentType() {
        return datacontenttype;
    }

    public byte[] getData() {
        return data;
    }

    public UUID getReqId() {
        return reqId;
    }


    public static class MicroCloudEventBuilder {
        private UUri sink;
        private UUri source;
        private Type type; 
        private Priority priority;
        private Integer ttl;
        private String dataschema;
        private String datacontenttype;
        private byte[] data;
        private UUID reqId;

        public MicroCloudEventBuilder() {}
        
        public MicroCloudEventBuilder withSink(UUri sink) {
            this.sink = sink;
            return this;
        }

        public MicroCloudEventBuilder withSource(UUri source) {
            this.source = source;
            return this;
        }
        
        public MicroCloudEventBuilder withTtl(Integer ttl) {
            this.ttl = ttl;
            return this;
        }
        
        public MicroCloudEventBuilder withType(Type type) {
            this.type = type;
            return this;
        }
        
        public MicroCloudEventBuilder withPriority(Priority priority) {
            this.priority = priority;
            return this;
        }
        
        public MicroCloudEventBuilder withDataSchema(String dataschema) {
            this.dataschema = dataschema;
            return this;
        }
        
        public MicroCloudEventBuilder withDataContentType(String datacontenttype) {
            this.datacontenttype = datacontenttype;
            return this;
        }

        public MicroCloudEventBuilder withData(byte[] data) {
            this.data = data;
            return this;
        }

        public MicroCloudEventBuilder withReqId(UUID id) {
            this.reqId = id;
            return this;
        }

        public MicroCloudEvent build() {
            // validation if needed
            return new MicroCloudEvent(this);
        }
    }
}

