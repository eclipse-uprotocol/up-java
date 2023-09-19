package org.eclipse.uprotocol.uri.datamodel;

/**
 * Interface used to provide hints as to how the Uri part or Uri itself is formated meaning does it contain 
 * long format, micro format or both (resolved) information. 
 */
public interface UriFormat {

    /**
     * Supporting empty Uri parts enables avoiding null values in the data model, and can indicate the absence of a Uri Part.
     * @return Returns true if the Uri part is empty.
     */
    boolean isEmpty();

    /**
     * Returns true if the Uri part contains both names and ids (numbers) corresponding to the data inside its belly.
     * isResolved true means that the Uri part can be serialized to both long and micro uri format.
     * @return Returns true if the Uri part contains both names and ids (numbers), long and micro representations.
     */
    boolean isResolved();

    /**
     * Returns true if the Uri part contains names so that it can be serialized to long format.
     * @return Returns true if the Uri part contains names so that it can be serialized to long format.
     */
    boolean isLongForm();

    /**
     * Returns true if the Uri part contains the id's which will allow the Uri part to be serialized into micro form.
     * @return Returns true if the Uri part can be serialized into micro form.
     */
    boolean isMicroForm();
}
