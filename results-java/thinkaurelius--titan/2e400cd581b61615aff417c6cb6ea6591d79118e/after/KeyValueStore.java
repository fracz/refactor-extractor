package com.thinkaurelius.titan.diskstorage.util;

import com.thinkaurelius.titan.diskstorage.TransactionHandle;
import com.thinkaurelius.titan.exceptions.GraphStorageException;

import java.nio.ByteBuffer;
import java.util.List;

public interface KeyValueStore {

	public void insert(List<KeyValueEntry> entries, TransactionHandle txh);


	public void delete(List<ByteBuffer> keys, TransactionHandle txh);


	public ByteBuffer get(ByteBuffer key, TransactionHandle txh);


	public boolean containsKey(ByteBuffer key, TransactionHandle txh);


	public boolean isLocalKey(ByteBuffer key);


	public void acquireLock(ByteBuffer key, TransactionHandle txh);

	/**
	 * Closes the store.
	 *
	 * @throws GraphStorageException
	 */
	public void close() throws GraphStorageException;

}