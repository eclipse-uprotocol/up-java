package org.eclipse.uprotocol.uri.datamodel;

/**
 * UriFormat is the interface that represents either Long, Short, or micro formats of a uProtocol Uri.
 * UriFormat is passed to the transports such that a transport can fix what format they expect to use.
 * 
 * @param <T> The type used for the uProtocol Uri, long and short use String while micro is byte[]
 */
public abstract class UriFormat<T> {
    protected final T uProtocolUri;
    private final UUri uuri;

    /**
     * Constructor for UriFormat called by the child classes (Short, Long, Micro)
     * @param uProtocolUri Format specific generated uProtocol URI
     * @param uuri UUri data object
     */
    protected UriFormat(T uProtocolUri, UUri uuri) {
        this.uProtocolUri = uProtocolUri;
        this.uuri = uuri;
    }

    /**
     * Return the uProtocol URI in the correct expected format
     * @return uProtocol URI
     */
    public T uProtocolUri() {
        return uProtocolUri;
    }

    /**
     * Obtain the UUri that was used to build the Uri Format
     * This method might be used if we want to convert from one format to another
     * @return UUri data object
     */
    public UUri uuri() {
        return uuri;
    }

    /**
     * Check if the UProtocolUri is empty. An empty Uri means that we were not able to 
     * build it from the UUri. 
     * @return  true if empty, false otherwise
     */
    abstract boolean isEmpty();
}
