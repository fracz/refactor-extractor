package com.thinkaurelius.titan.diskstorage;

/**
 * This exception signifies a (potentially) temporary exception while attempting to acquire a lock
 * in the Titan storage backend.
 *
 *
 * (c) Matthias Broecheler (me@matthiasb.com)
 */

public class TemporaryLockingException extends TemporaryStorageException implements LockingException  {

    private static final long serialVersionUID = 482890657293484420L;

    /**
     *
     * @param msg Exception message
     */
    public TemporaryLockingException(String msg) {
        super(msg);
    }

    /**
     *
     * @param msg Exception message
     * @param cause Cause of the exception
     */
    public TemporaryLockingException(String msg, Throwable cause) {
        super(msg,cause);
    }

    /**
     * Constructs an exception with a generic message
     *
     * @param cause Cause of the exception
     */
    public TemporaryLockingException(Throwable cause) {
        this("Temporary locking failure",cause);
    }



}