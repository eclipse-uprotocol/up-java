package org.eclipse.uprotocol.transport.builder;


import org.eclipse.uprotocol.v1.*;

/**
 * Builder for easy construction of the UAttributes object.
 */
public class UAttributesBuilder {

    private final UUID id;
    private final UMessageType type;
    private final UPriority priority;
    private Integer ttl;
    private String token;
    private UUri sink;
    private Integer plevel;
    private Integer commstatus;
    private UUID reqid;

    /**
     * Construct the UAttributesBuilder with the configurations that are required for every payload transport.
     *
     * @param id       Unique identifier for the message.
     * @param type     Message type such as Publish a state change, RPC request or RPC response.
     * @param priority uProtocol Prioritization classifications.
     */
    public UAttributesBuilder(UUID id, UMessageType type, UPriority priority) {
        this.id = id;
        this.type = type;
        this.priority = priority;
    }

    /**
     * Add the time to live in milliseconds.
     *
     * @param ttl the time to live in milliseconds.
     * @return Returns the UAttributesBuilder with the configured ttl.
     */
    public UAttributesBuilder withTtl(Integer ttl) {
        this.ttl = ttl;
        return this;
    }

    /**
     * Add the authorization token used for TAP.
     *
     * @param token the authorization token used for TAP.
     * @return Returns the UAttributesBuilder with the configured token.
     */
    public UAttributesBuilder withToken(String token) {
        this.token = token;
        return this;
    }

    /**
     * Add the explicit destination URI.
     *
     * @param sink the explicit destination URI.
     * @return Returns the UAttributesBuilder with the configured sink.
     */
    public UAttributesBuilder withSink(UUri sink) {
        this.sink = sink;
        return this;
    }

    /**
     * Add the permission level of the message.
     *
     * @param plevel the permission level of the message.
     * @return Returns the UAttributesBuilder with the configured plevel.
     */
    public UAttributesBuilder withPermissionLevel(Integer plevel) {
        this.plevel = plevel;
        return this;
    }

    /**
     * Add the communication status of the message.
     *
     * @param commstatus the communication status of the message.
     * @return Returns the UAttributesBuilder with the configured commstatus.
     */
    public UAttributesBuilder withCommStatus(Integer commstatus) {
        this.commstatus = commstatus;
        return this;
    }

    /**
     * Add the request ID.
     *
     * @param reqid the request ID.
     * @return Returns the UAttributesBuilder with the configured reqid.
     */
    public UAttributesBuilder withReqId(UUID reqid) {
        this.reqid = reqid;
        return this;
    }

    /**
     * Construct the UAttributes from the builder.
     *
     * @return Returns a constructed
     */
    public UAttributes build() {
        UAttributes.Builder attributesBuilder=UAttributes.newBuilder();
        if(id!=null){
            attributesBuilder.setId(id);
        }
        if(type!=null){
            attributesBuilder.setType(type);
        }
        if(sink!=null){
            attributesBuilder.setSink(sink);
        }
        if(priority!=null){
            attributesBuilder.setPriority(priority);
        }
        if(ttl!=null){
            attributesBuilder.setTtl(ttl);
        }
        if(plevel!=null){
            attributesBuilder.setPermissionLevel(plevel);
        }
        if(commstatus!=null){
            attributesBuilder.setCommstatus(commstatus);
        }
        if(reqid!=null){
            attributesBuilder.setReqid(reqid);
        }
        if(token!=null){
            attributesBuilder.setToken(token);
        }
        return attributesBuilder.build();

    }
}
