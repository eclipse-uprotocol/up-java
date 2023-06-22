package org.eclipse.uprotocol.topic;

public class ShortUResource implements UResource<Integer> {
    
        private final Integer resource;
    
        public ShortUResource(Integer resource) {
            this.resource = resource;
        }
    
        @Override
        public Integer getResource() {
            return this.resource;
        }

        @Override
        public boolean isRpcMethod() {
            return (this.resource < 100);
        }
    
}
