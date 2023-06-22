package org.eclipse.uprotocol.topic;

public class LongUResource implements UResource<String> {
    
        private final org.eclipse.uprotocol.uri.datamodel.UResource resource;
    
        public LongUResource(org.eclipse.uprotocol.uri.datamodel.UResource resource) {
            this.resource = resource;
        }
    
        @Override
        public String getResource() {
            return this.resource.nameWithInstance();
        }

        @Override
        public boolean isRpcMethod() {
            return this.resource.isRPCMethod();
        }
    
}
