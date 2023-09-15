package org.eclipse.uprotocol.uri.datamodel;

/**
 * Basic building blocks for all objects that are art of a uProtocol URI. The
 * interface defines APIs that MUST be implemented in all objects.
 */
public interface Uri {

    /**
     * Return true if the object is empty
     * @return true if the object is empty
     */
    public boolean isEmpty();

    
    /**
     * Check if the passed object is equal to this object
     * @param o The object to compare
     * @return true if the passed object is equal to this object
     */
    public boolean equals(Object o);

    /**
     * Return the hash code of this object
     * @return the hash code of this object
     */
    public int hashCode();

    /**
     * Return the string representation of this object
     * @return the string representation of this object
     */
    public String toString();

    /**
     * Returns true if the object contains both names and ids (numbers) corresponding to the
     * data inside of its belly. Return of true means that the object can be serialized to both
     * long and micro uri format.
     * @return Returns true if the object contains both names and ids (numbers)
     */
    public boolean isResolved();

    /**
     * Returns true if the object contains names so that it can be serialized to long format.
     * @return Returns true if the object contains names so that it can be serialized to long format.
     */
    public boolean isLongForm();
}
