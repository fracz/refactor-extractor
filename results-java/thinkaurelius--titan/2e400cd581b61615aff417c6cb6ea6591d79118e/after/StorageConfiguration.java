package com.thinkaurelius.titan.configuration;

import com.thinkaurelius.titan.diskstorage.OrderedKeyColumnValueStore;
import com.thinkaurelius.titan.diskstorage.StorageManager;
import com.thinkaurelius.titan.exceptions.GraphStorageException;
import org.apache.commons.configuration.Configuration;

import java.io.File;

/**
 * Configuration of the storage backend used by the graph database.
 * Every storage backend implementation must also provide a storage
 * configuration implementation, so that the graph database middleware
 * knows how to interact with the storage backend.
 *
 * @author	Matthias Br&ouml;cheler (me@matthiasb.com);
 *
 */
public interface StorageConfiguration {

	/**
	 * Default name for the edge OrderedKeyColumnValueStore.
	 */
	public static final String edgeStoreName = "edgeStore";

	/**
	 * Default name for the property OrderedKeyColumnValueStore.
	 */
	public static final String propertyIndexName = "propertyIndex";

	/**
	 * Opens a new storage manager for this backend storage type.
	 *
	 * @param directory Directory to create database in
	 * @param readOnly Whether the storage manager should be read-only
	 * @return A storage manager of this type in the specified directory.
	 * @throws java.io.IOException if the directory cannot be accessed
	 * @throws GraphStorageException if there is an error when initializing the storage maanger.
	 *
	 * @see StorageManager
	 */
	public StorageManager getStorageManager(File directory, boolean readOnly);

	/**
	 * Opens an {@link OrderedKeyColumnValueStore} for edge storage in the provided storage manager
	 *
	 * @param manager Storage Manager to create edge store in
	 * @return Store for edges
	 */
	public OrderedKeyColumnValueStore getEdgeStore(StorageManager manager);

	/**
	 * Opens an {@link OrderedKeyColumnValueStore} for property indexing in the provided storage manager
	 *
	 * @param manager Storage Manager to create property hasIndex in
	 * @return Index for properties
	 */
	public OrderedKeyColumnValueStore getPropertyIndex(StorageManager manager);

	/**
	 * Load configuration parameters from the provided configuration if an existing
	 * database has been opened. In this case, parameters set by the user might get overwritten
	 * by those found in the configuration file if these parameters cannot be changed on an
	 * existing database.
	 * If the database is new, default parameters or those set by the users should be used.
	 *
	 * @param config Configuration to read configuration parameters from. This configuration may be empty.
	 */
	public void open(Configuration config);

	/**
	 * Save current configuration parameters for this storage configuration in the provided configuration.
	 *
	 * @param config Configuration to write configuration parameters to.
	 */
	public void save(Configuration config);

}