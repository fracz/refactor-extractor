/*
 * Copyright 1999-2010 Luca Garulli (l.garulli--at--orientechnologies.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.orientechnologies.orient.client.remote;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.orientechnologies.common.exception.OException;
import com.orientechnologies.common.io.OIOException;
import com.orientechnologies.common.log.OLogManager;
import com.orientechnologies.common.profiler.OProfiler;
import com.orientechnologies.common.util.OPair;
import com.orientechnologies.orient.client.distributed.OClientClusterManager;
import com.orientechnologies.orient.core.Orient;
import com.orientechnologies.orient.core.command.OCommandRequestAsynch;
import com.orientechnologies.orient.core.command.OCommandRequestText;
import com.orientechnologies.orient.core.config.OContextConfiguration;
import com.orientechnologies.orient.core.config.OGlobalConfiguration;
import com.orientechnologies.orient.core.config.OStorageConfiguration;
import com.orientechnologies.orient.core.db.record.ODatabaseRecord;
import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.exception.OCommandExecutionException;
import com.orientechnologies.orient.core.exception.ODatabaseException;
import com.orientechnologies.orient.core.exception.OStorageException;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.record.ORecord;
import com.orientechnologies.orient.core.record.ORecordFactory;
import com.orientechnologies.orient.core.record.ORecordInternal;
import com.orientechnologies.orient.core.record.ORecordSchemaAware;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.serialization.OSerializableStream;
import com.orientechnologies.orient.core.serialization.serializer.record.string.ORecordSerializerStringAbstract;
import com.orientechnologies.orient.core.serialization.serializer.stream.OStreamSerializerAnyStreamable;
import com.orientechnologies.orient.core.storage.OCluster;
import com.orientechnologies.orient.core.storage.ORawBuffer;
import com.orientechnologies.orient.core.storage.OStorage;
import com.orientechnologies.orient.core.storage.OStorageAbstract;
import com.orientechnologies.orient.core.tx.OTransaction;
import com.orientechnologies.orient.core.tx.OTransactionAbstract;
import com.orientechnologies.orient.core.tx.OTransactionRealAbstract;
import com.orientechnologies.orient.core.tx.OTransactionRecordEntry;
import com.orientechnologies.orient.enterprise.channel.binary.OChannelBinaryClient;
import com.orientechnologies.orient.enterprise.channel.binary.OChannelBinaryProtocol;

/**
 * This object is bound to each remote ODatabase instances.
 */
public class OStorageRemote extends OStorageAbstract {
	private static final String											DEFAULT_HOST								= "localhost";
	private static final String[]										DEFAULT_PORTS								= new String[] { "2424" };
	private static final String											ADDRESS_SEPARATOR						= ";";

	public static final String											PARAM_MIN_POOL							= "minpool";
	public static final String											PARAM_MAX_POOL							= "maxpool";

	private OStorageRemoteServiceThread							serviceThread;
	private OContextConfiguration										clientConfiguration;
	private int																			connectionRetry;
	private int																			connectionRetryDelay;

	private static List<OChannelBinaryClient>				networkPool									= new ArrayList<OChannelBinaryClient>();
	protected List<OPair<String, String[]>>					serverURLs									= new ArrayList<OPair<String, String[]>>();
	protected int																		retry												= 0;
	protected final Map<String, Integer>						clustersIds									= new HashMap<String, Integer>();
	protected final Map<String, String>							clustersTypes								= new HashMap<String, String>();
	protected int																		defaultClusterId;
	private int																			networkPoolCursor						= 0;
	private int																			minPool;
	private int																			maxPool;
	private final boolean														debug												= false;
	private ODocument																clusterConfiguration;
	private final List<ORemoteServerEventListener>	remoteServerEventListeners	= new ArrayList<ORemoteServerEventListener>();

	public OStorageRemote(final String iURL, final String iMode) throws IOException {
		super(iURL, iURL, iMode);
		configuration = null;

		clientConfiguration = new OContextConfiguration();
		connectionRetry = clientConfiguration.getValueAsInteger(OGlobalConfiguration.NETWORK_SOCKET_RETRY);
		connectionRetryDelay = clientConfiguration.getValueAsInteger(OGlobalConfiguration.NETWORK_SOCKET_RETRY_DELAY);

		parseServerURLs();
	}

	public int getSessionId() {
		return OStorageRemoteThreadLocal.INSTANCE.get().sessionId.intValue();
	}

	public void setSessionId(final int iSessionId) {
		OStorageRemoteThreadLocal.INSTANCE.get().sessionId = iSessionId;
	}

	public List<ORemoteServerEventListener> getRemoteServerEventListeners() {
		return remoteServerEventListeners;
	}

	public void addRemoteServerEventListener(final ORemoteServerEventListener iListener) {
		remoteServerEventListeners.add(iListener);
	}

	public void removeRemoteServerEventListener(final ORemoteServerEventListener iListener) {
		remoteServerEventListeners.remove(iListener);
	}

	public void open(final String iUserName, final String iUserPassword, final Map<String, Object> iOptions) {
		addUser();

		lock.acquireExclusiveLock();

		try {
			openRemoteDatabase(iUserName, iUserPassword, iOptions);

			configuration = new OStorageConfiguration(this);
			configuration.load();

		} catch (Exception e) {
			if (!OGlobalConfiguration.STORAGE_KEEP_OPEN.getValueAsBoolean())
				close();

			if (e instanceof OException)
				// PASS THROUGH
				throw (OException) e;
			else
				throw new OStorageException("Can't open the remote storage: " + name, e);

		} finally {
			lock.releaseExclusiveLock();
		}
	}

	public void create(final Map<String, Object> iOptions) {
		throw new UnsupportedOperationException(
				"Can't create a database in a remote server. Please use the console or the OServerAdmin class.");
	}

	public boolean exists() {
		throw new UnsupportedOperationException(
				"Can't check the existance of a database in a remote server. Please use the console or the OServerAdmin class.");
	}

	public void close(final boolean iForce) {
		lock.acquireExclusiveLock();

		OChannelBinaryClient network = null;
		try {

			try {
				network = beginRequest(OChannelBinaryProtocol.REQUEST_DB_CLOSE);
			} finally {
				endRequest(network);
			}

			getResponse(network);

			OStorageRemoteThreadLocal.INSTANCE.get().sessionId = -1;

			if (!checkForClose(iForce))
				return;

			// CLOSE THE CHANNEL
			if (serviceThread != null) {
				serviceThread.sendShutdown();
				serviceThread.interrupt();
			}

			synchronized (networkPool) {
				for (OChannelBinaryClient n : networkPool)
					n.close();
				networkPool.clear();
			}

			level2Cache.shutdown();

			Orient.instance().unregisterStorage(this);
			open = false;

		} catch (OException e) {
			// PASS THROUGH
			throw e;
		} catch (Exception e) {
			OLogManager.instance().debug(this, "Error on closing remote connection: %s", network);
		} finally {

			lock.releaseExclusiveLock();
		}
	}

	public void delete() {
		throw new UnsupportedOperationException(
				"Can't delete a database in a remote server. Please use the console or the OServerAdmin class.");
	}

	public Set<String> getClusterNames() {
		lock.acquireSharedLock();

		try {
			checkConnection();
			return clustersIds.keySet();

		} finally {
			lock.releaseSharedLock();
		}
	}

	/**
	 * Method that completes the cluster rename operation. <strong>IT WILL NOT RENAME A CLUSTER, IT JUST CHANGES THE NAME IN THE
	 * INTERNAL MAPPING</strong>
	 */
	public void renameCluster(String iOldName, String iNewName) {
		lock.acquireSharedLock();

		try {
			checkConnection();
			final Integer clusterId = clustersIds.remove(iOldName);
			clustersIds.put(iNewName, clusterId);

		} finally {
			lock.releaseSharedLock();
		}
	}

	public long createRecord(final ORecordId iRid, final byte[] iContent, final byte iRecordType) {
		checkConnection();

		do {
			try {

				OChannelBinaryClient network = null;
				try {
					network = beginRequest(OChannelBinaryProtocol.REQUEST_RECORD_CREATE);

					network.writeShort((short) iRid.clusterId);
					network.writeBytes(iContent);
					network.writeByte(iRecordType);

				} finally {
					endRequest(network);
				}

				try {
					beginResponse(network);

					iRid.clusterPosition = network.readLong();
					return iRid.clusterPosition;

				} finally {
					endResponse(network);
				}

			} catch (OException e) {
				// PASS THROUGH
				throw e;
			} catch (Exception e) {
				handleException("Error on create record in cluster: " + iRid.clusterId, e);

			}
		} while (true);
	}

	public ORawBuffer readRecord(final ODatabaseRecord iDatabase, final ORecordId iRid, final String iFetchPlan) {
		checkConnection();

		if (OStorageRemoteThreadLocal.INSTANCE.get().commandExecuting)
			// PENDING NETWORK OPERATION, CAN'T EXECUTE IT NOW
			return null;

		do {
			try {

				OChannelBinaryClient network = null;
				try {

					network = beginRequest(OChannelBinaryProtocol.REQUEST_RECORD_LOAD);
					network.writeRID(iRid);
					network.writeString(iFetchPlan != null ? iFetchPlan : "");

				} finally {
					endRequest(network);
				}

				try {
					beginResponse(network);

					if (network.readByte() == 0)
						return null;

					final ORawBuffer buffer = new ORawBuffer(network.readBytes(), network.readInt(), network.readByte());

					ORecordInternal<?> record;
					while (network.readByte() == 2) {
						record = (ORecordInternal<?>) readIdentifiable(network, iDatabase);

						// PUT IN THE CLIENT LOCAL CACHE
						iDatabase.getLevel1Cache().updateRecord(record);
					}
					return buffer;
				} finally {
					endResponse(network);
				}

			} catch (OException e) {
				// PASS THROUGH
				throw e;
			} catch (Exception e) {
				handleException("Error on read record #" + iRid, e);

			}
		} while (true);
	}

	public int updateRecord(final ORecordId iRid, final byte[] iContent, final int iVersion, final byte iRecordType) {
		checkConnection();

		do {
			try {
				OChannelBinaryClient network = null;
				try {
					network = beginRequest(OChannelBinaryProtocol.REQUEST_RECORD_UPDATE);

					network.writeRID(iRid);
					network.writeBytes(iContent);
					network.writeInt(iVersion);
					network.writeByte(iRecordType);

				} finally {
					endRequest(network);
				}

				try {
					beginResponse(network);
					return network.readInt();
				} finally {
					endResponse(network);
				}

			} catch (OException e) {
				// PASS THROUGH
				throw e;
			} catch (Exception e) {
				handleException("Error on update record #" + iRid, e);

			}
		} while (true);
	}

	public boolean deleteRecord(final ORecordId iRid, final int iVersion) {
		checkConnection();

		do {
			try {
				OChannelBinaryClient network = null;
				try {
					network = beginRequest(OChannelBinaryProtocol.REQUEST_RECORD_DELETE);

					network.writeRID(iRid);
					network.writeInt(iVersion);

				} finally {
					endRequest(network);
				}

				try {
					beginResponse(network);
					return network.readByte() == 1;
				} finally {
					endResponse(network);
				}

			} catch (OException e) {
				// PASS THROUGH
				throw e;
			} catch (Exception e) {
				handleException("Error on delete record #" + iRid, e);

			}
		} while (true);
	}

	public long count(final int iClusterId) {
		return count(new int[] { iClusterId });
	}

	public long[] getClusterDataRange(final int iClusterId) {
		checkConnection();

		do {
			try {
				OChannelBinaryClient network = null;
				try {
					network = beginRequest(OChannelBinaryProtocol.REQUEST_DATACLUSTER_DATARANGE);

					network.writeShort((short) iClusterId);

				} finally {
					endRequest(network);
				}

				try {
					beginResponse(network);
					return new long[] { network.readLong(), network.readLong() };
				} finally {
					endResponse(network);
				}

			} catch (OException e) {
				// PASS THROUGH
				throw e;
			} catch (Exception e) {
				handleException("Error on getting last entry position count in cluster: " + iClusterId, e);

			}
		} while (true);
	}

	public long getSize() {
		checkConnection();

		do {
			try {
				OChannelBinaryClient network = null;
				try {

					network = beginRequest(OChannelBinaryProtocol.REQUEST_DB_SIZE);

				} finally {
					endRequest(network);
				}

				try {
					beginResponse(network);
					return network.readLong();
				} finally {
					endResponse(network);
				}
			} catch (OException e) {
				// PASS THROUGH
				throw e;
			} catch (Exception e) {
				handleException("Error on read database size", e);

			}
		} while (true);
	}

	@Override
	public long countRecords() {
		checkConnection();

		do {
			try {
				OChannelBinaryClient network = null;
				try {

					network = beginRequest(OChannelBinaryProtocol.REQUEST_DB_COUNTRECORDS);

				} finally {
					endRequest(network);
				}

				try {
					beginResponse(network);
					return network.readLong();
				} finally {
					endResponse(network);
				}
			} catch (OException e) {
				// PASS THROUGH
				throw e;
			} catch (Exception e) {
				handleException("Error on read database record count", e);

			}
		} while (true);
	}

	public long count(final int[] iClusterIds) {
		checkConnection();

		do {
			try {
				OChannelBinaryClient network = null;
				try {
					network = beginRequest(OChannelBinaryProtocol.REQUEST_DATACLUSTER_COUNT);

					network.writeShort((short) iClusterIds.length);
					for (int i = 0; i < iClusterIds.length; ++i)
						network.writeShort((short) iClusterIds[i]);

				} finally {
					endRequest(network);
				}

				try {
					beginResponse(network);
					return network.readLong();
				} finally {
					endResponse(network);
				}
			} catch (OException e) {
				// PASS THROUGH
				throw e;
			} catch (Exception e) {
				handleException("Error on read record count in clusters: " + iClusterIds, e);

			}
		} while (true);
	}

	public long count(final String iClassName) {
		checkConnection();

		do {

			try {
				OChannelBinaryClient network = null;
				try {

					network = beginRequest(OChannelBinaryProtocol.REQUEST_COUNT);
					network.writeString(iClassName);

				} finally {
					endRequest(network);
				}

				try {
					beginResponse(network);
					return network.readLong();
				} finally {
					endResponse(network);
				}
			} catch (OException e) {
				// PASS THROUGH
				throw e;
			} catch (Exception e) {
				handleException("Error on executing count on class: " + iClassName, e);

			}
		} while (true);
	}

	/**
	 * Execute the command remotely and get the results back.
	 */
	public Object command(final OCommandRequestText iCommand) {
		checkConnection();

		if (!(iCommand instanceof OSerializableStream))
			throw new OCommandExecutionException("Can't serialize the command to being executed to the server side.");

		OSerializableStream command = iCommand;
		Object result = null;

		do {
			OStorageRemoteThreadLocal.INSTANCE.get().commandExecuting = true;

			try {
				final OCommandRequestText aquery = iCommand;

				final boolean asynch = iCommand instanceof OCommandRequestAsynch;

				OChannelBinaryClient network = null;
				try {
					network = beginRequest(OChannelBinaryProtocol.REQUEST_COMMAND);

					network.writeByte((byte) (asynch ? 'a' : 's')); // ASYNC / SYNC
					network.writeBytes(OStreamSerializerAnyStreamable.INSTANCE.toStream(iCommand.getDatabase(), command));

				} finally {
					endRequest(network);
				}

				try {
					beginResponse(network);

					if (asynch) {
						byte status;

						// ASYNCH: READ ONE RECORD AT TIME
						while ((status = network.readByte()) > 0) {
							ORecordSchemaAware<?> record = (ORecordSchemaAware<?>) readIdentifiable(network, iCommand.getDatabase());
							if (record == null)
								break;

							switch (status) {
							case 1:
								// PUT AS PART OF THE RESULT SET. INVOKE THE LISTENER
								try {
									if (!aquery.getResultListener().result(record)) {
										// EMPTY THE INPUT CHANNEL
										while (network.in.available() > 0)
											network.in.read();

										break;
									}
								} catch (Throwable t) {
									// ABSORBE ALL THE USER EXCEPTIONS
									t.printStackTrace();
								}
								iCommand.getDatabase().getLevel1Cache().updateRecord(record);
								break;

							case 2:
								// PUT IN THE CLIENT LOCAL CACHE
								iCommand.getDatabase().getLevel1Cache().updateRecord(record);
							}
						}
					} else {
						final byte type = network.readByte();
						switch (type) {
						case 'n':
							result = null;
							break;

						case 'r':
							result = readIdentifiable(network, iCommand.getDatabase());
							if (result instanceof ORecord<?>)
								iCommand.getDatabase().getLevel1Cache().updateRecord((ORecordInternal<?>) result);
							break;

						case 'l':
							final int tot = network.readInt();
							final Collection<OIdentifiable> list = new ArrayList<OIdentifiable>();
							for (int i = 0; i < tot; ++i) {
								final OIdentifiable resultItem = readIdentifiable(network, iCommand.getDatabase());
								if (resultItem instanceof ORecord<?>)
									iCommand.getDatabase().getLevel1Cache().updateRecord((ORecordInternal<?>) resultItem);
								list.add(resultItem);
							}
							result = list;
							break;

						case 'a':
							final String value = new String(network.readBytes());
							result = ORecordSerializerStringAbstract.fieldTypeFromStream(null, ORecordSerializerStringAbstract.getType(value),
									value);
							break;
						}
					}
					break;
				} finally {
					endResponse(network);
				}

			} catch (OException e) {
				// PASS THROUGH
				throw e;
			} catch (Exception e) {
				handleException("Error on executing command: " + iCommand, e);

			} finally {
				OStorageRemoteThreadLocal.INSTANCE.get().commandExecuting = false;
			}
		} while (true);

		return result;
	}

	public void commit(final OTransaction iTx) {
		checkConnection();

		do {
			try {
				final Set<OTransactionRecordEntry> allEntries = new HashSet<OTransactionRecordEntry>();

				OChannelBinaryClient network = null;
				try {
					network = beginRequest(OChannelBinaryProtocol.REQUEST_TX_COMMIT);

					network.writeInt(((OTransactionRealAbstract) iTx).getId());
					network.writeByte((byte) (((OTransactionRealAbstract) iTx).isUsingLog() ? 1 : 0));

					final List<OTransactionRecordEntry> tmpEntries = new ArrayList<OTransactionRecordEntry>();

					while (iTx.getRecordEntries().iterator().hasNext()) {
						for (OTransactionRecordEntry txEntry : iTx.getRecordEntries())
							if (!allEntries.contains(txEntry))
								tmpEntries.add(txEntry);

						iTx.clearRecordEntries();

						if (tmpEntries.size() > 0) {
							for (OTransactionRecordEntry txEntry : tmpEntries)
								commitEntry(network, txEntry);

							allEntries.addAll(tmpEntries);
							tmpEntries.clear();
						}

					}

					// END OF RECORD ENTRIES
					network.writeByte((byte) 0);

					// SEND INDEX ENTRIES
					network.writeBytes(iTx.getIndexChanges().toStream());
				} finally {
					endRequest(network);
				}

				try {
					beginResponse(network);
					final int createdRecords = network.readInt();
					ORecordId currentRid;
					ORecordId createdRid;
					for (int i = 0; i < createdRecords; i++) {
						currentRid = network.readRID();
						createdRid = network.readRID();
						for (OTransactionRecordEntry txEntry : allEntries) {
							if (txEntry.getRecord().getIdentity().equals(currentRid)) {
								txEntry.getRecord().setIdentity(createdRid);
								break;
							}
						}
					}
					final int updatedRecords = network.readInt();
					ORecordId rid;
					for (int i = 0; i < updatedRecords; ++i) {
						rid = network.readRID();

						// SEARCH THE RECORD WITH THAT ID TO UPDATE THE VERSION
						for (OTransactionRecordEntry txEntry : allEntries) {
							if (txEntry.getRecord().getIdentity().equals(rid)) {
								txEntry.getRecord().setVersion(network.readInt());
								break;
							}
						}
					}
				} finally {
					endResponse(network);
				}

				// UPDATE THE CACHE ONLY IF THE ITERATOR ALLOWS IT
				OTransactionAbstract.updateCacheFromEntries(this, iTx, allEntries);

				break;
			} catch (OException e) {
				// PASS THROUGH
				throw e;
			} catch (Exception e) {
				handleException("Error on commit", e);

			}
		} while (true);
	}

	public void rollback(OTransaction iTx) {
	}

	public int getClusterIdByName(final String iClusterName) {
		checkConnection();

		if (iClusterName == null)
			return -1;

		if (Character.isDigit(iClusterName.charAt(0)))
			return Integer.parseInt(iClusterName);

		final Integer id = clustersIds.get(iClusterName.toLowerCase());
		if (id == null)
			return -1;

		return id;
	}

	public String getClusterTypeByName(final String iClusterName) {
		checkConnection();

		if (iClusterName == null)
			return null;

		return clustersTypes.get(iClusterName.toLowerCase());
	}

	public int getDefaultClusterId() {
		return defaultClusterId;
	}

	public int addCluster(final String iClusterName, final OStorage.CLUSTER_TYPE iClusterType, final Object... iArguments) {
		checkConnection();

		do {
			try {
				OChannelBinaryClient network = null;
				try {
					network = beginRequest(OChannelBinaryProtocol.REQUEST_DATACLUSTER_ADD);

					network.writeString(iClusterType.toString());
					network.writeString(iClusterName);

					switch (iClusterType) {
					case PHYSICAL:
						// FILE PATH + START SIZE
						network.writeString(iArguments.length > 0 ? (String) iArguments[0] : "").writeInt(
								iArguments.length > 0 ? (Integer) iArguments[1] : -1);
						break;

					case LOGICAL:
						// PHY CLUSTER ID
						network.writeInt(iArguments.length > 0 ? (Integer) iArguments[0] : -1);
						break;
					}
				} finally {
					endRequest(network);
				}

				try {
					beginResponse(network);
					final int clusterId = network.readShort();

					clustersIds.put(iClusterName.toLowerCase(), clusterId);
					clustersTypes.put(iClusterName.toLowerCase(), iClusterType.toString());
					return clusterId;
				} finally {
					endResponse(network);
				}
			} catch (OException e) {
				// PASS THROUGH
				throw e;
			} catch (Exception e) {
				handleException("Error on add new cluster", e);

			}
		} while (true);
	}

	public boolean dropCluster(final int iClusterId) {
		checkConnection();

		do {
			try {
				OChannelBinaryClient network = null;
				try {
					network = beginRequest(OChannelBinaryProtocol.REQUEST_DATACLUSTER_REMOVE);

					network.writeShort((short) iClusterId);

				} finally {
					endRequest(network);
				}

				try {
					beginResponse(network);

					if (network.readByte() == 1) {
						// REMOVE THE CLUSTER LOCALLY
						for (Entry<String, Integer> entry : clustersIds.entrySet())
							if (entry.getValue() != null && entry.getValue().intValue() == iClusterId) {
								clustersIds.remove(entry.getKey());
								clustersTypes.remove(entry.getKey());
								if (configuration.clusters.size() > iClusterId)
									configuration.clusters.set(iClusterId, null);
								break;
							}
						getLevel2Cache().freeCluster(iClusterId);
						return true;
					}
					return false;
				} finally {
					endResponse(network);
				}

			} catch (OException e) {
				// PASS THROUGH
				throw e;
			} catch (Exception e) {
				handleException("Error on removing of cluster", e);

			}
		} while (true);
	}

	public int addDataSegment(final String iDataSegmentName) {
		return addDataSegment(iDataSegmentName, null);
	}

	public int addDataSegment(final String iSegmentName, final String iSegmentFileName) {
		checkConnection();

		do {
			try {
				OChannelBinaryClient network = null;
				try {
					network = beginRequest(OChannelBinaryProtocol.REQUEST_DATASEGMENT_ADD);

					network.writeString(iSegmentName).writeString(iSegmentFileName);

				} finally {
					endRequest(network);
				}

				try {
					beginResponse(network);
					return network.readShort();
				} finally {
					endResponse(network);
				}

			} catch (OException e) {
				// PASS THROUGH
				throw e;
			} catch (Exception e) {
				handleException("Error on add new data segment", e);

			}
		} while (true);
	}

	public void synch() {
	}

	public String getPhysicalClusterNameById(final int iClusterId) {
		for (Entry<String, Integer> clusterEntry : clustersIds.entrySet()) {
			if (clusterEntry.getValue().intValue() == iClusterId)
				return clusterEntry.getKey();
		}
		return null;
	}

	public Collection<OCluster> getClusters() {
		throw new UnsupportedOperationException("getClusters()");
	}

	public OCluster getClusterById(final int iId) {
		throw new UnsupportedOperationException("getClusterById()");
	}

	@Override
	public long getVersion() {
		throw new UnsupportedOperationException("getVersion");
	}

	public ODocument getClusterConfiguration() {
		return clusterConfiguration;
	}

	/**
	 * Handles exceptions. In case of IO errors retries to reconnect until the configured retry times has reached.
	 *
	 * @param iMessage
	 * @param iException
	 */
	protected void handleException(final String iMessage, final Exception iException) {
		if (iException instanceof OException)
			// RE-THROW IT
			throw (OException) iException;

		if (!(iException instanceof IOException))
			throw new OStorageException(iMessage, iException);

		while (retry < connectionRetry) {
			// WAIT THE DELAY BEFORE TO RETRY
			try {
				Thread.sleep(connectionRetryDelay);
			} catch (InterruptedException e) {
			}

			try {
				if (OLogManager.instance().isDebugEnabled())
					OLogManager.instance().debug(this, "Retrying to connect to remote server #" + retry + "/" + connectionRetry + "...");

				openRemoteDatabase(null, null, null);

				retry = 0;

				OLogManager.instance().info(this,
						"Connection re-acquired in transparent way: no errors will be thrown at application level");

				return;
			} catch (Throwable t) {
				++retry;
			}
		}

		retry = 0;

		// RECONNECTION FAILED: THROW+LOG THE ORIGINAL EXCEPTION
		throw new OStorageException(iMessage, iException);
	}

	protected void openRemoteDatabase(final String iUserName, final String iUserPassword, final Map<String, Object> iOptions)
			throws IOException {
		minPool = OGlobalConfiguration.CLIENT_CHANNEL_MIN_POOL.getValueAsInteger();
		maxPool = OGlobalConfiguration.CLIENT_CHANNEL_MAX_POOL.getValueAsInteger();

		if (iOptions != null && iOptions.size() > 0) {
			if (iOptions.containsKey(PARAM_MIN_POOL))
				minPool = Integer.parseInt(iOptions.get(PARAM_MIN_POOL).toString());
			if (iOptions.containsKey(PARAM_MAX_POOL))
				maxPool = Integer.parseInt(iOptions.get(PARAM_MAX_POOL).toString());
		}

		createConnectionPool();

		OChannelBinaryClient network = null;
		try {
			network = beginRequest(OChannelBinaryProtocol.REQUEST_DB_OPEN);

			network.writeString(name).writeString(iUserName).writeString(iUserPassword);

		} finally {
			endRequest(network);
		}

		final int sessionId;

		try {
			beginResponse(network);

			sessionId = network.readInt();

			if (debug)
				System.out.println("new sess: " + sessionId);

			OLogManager.instance().debug(null, "Client connected with session id: " + sessionId);

			int tot = network.readInt();
			String clusterName;
			for (int i = 0; i < tot; ++i) {
				clusterName = network.readString().toLowerCase();
				clustersIds.put(clusterName, network.readInt());
				clustersTypes.put(clusterName, network.readString());
			}

			// READ CLUSTER CONFIGURATION
			updateClusterConfiguration(network.readBytes());

		} finally {
			endResponse(network);
		}

		setSessionId(sessionId);

		defaultClusterId = clustersIds.get(OStorage.CLUSTER_DEFAULT_NAME);

		open = true;
	}

	/**
	 * Parse the URL in the following formats:<br/>
	 */
	protected void parseServerURLs() {
		String remoteHost;
		String[] remotePorts;

		int dbPos = url.indexOf('/');
		if (dbPos == -1) {
			// SHORT FORM
			name = url;
			remoteHost = getDefaultHost();
			remotePorts = getDefaultPort();
		} else {
			name = url.substring(dbPos + 1);

			int startPos = 0;
			int endPos = 0;

			while (endPos < dbPos) {
				if (url.indexOf(ADDRESS_SEPARATOR, startPos) > -1)
					endPos = url.indexOf(ADDRESS_SEPARATOR, startPos);
				else
					endPos = dbPos;

				int posRemotePort = url.indexOf(':', startPos);

				if (posRemotePort != -1 && posRemotePort < endPos) {
					remoteHost = url.substring(startPos, posRemotePort);
					remotePorts = url.substring(posRemotePort + 1, endPos).split("_");
					startPos = endPos + 1;
				} else {
					remoteHost = url.substring(startPos, endPos);
					remotePorts = getDefaultPort();
					startPos = endPos + 1;
				}

				// REGISTER THE REMOTE SERVER+PORT
				serverURLs.add(new OPair<String, String[]>(remoteHost, remotePorts));
			}
		}
	}

	protected String getDefaultHost() {
		return DEFAULT_HOST;
	}

	protected String[] getDefaultPort() {
		return DEFAULT_PORTS;
	}

	protected OChannelBinaryClient createNetworkConnection() throws IOException, UnknownHostException {
		int port;
		for (OPair<String, String[]> server : serverURLs) {
			port = Integer.parseInt(server.getValue()[server.getValue().length - 1]);

			OLogManager.instance().debug(this, "Trying to connect to the remote host %s:%d...", server.getKey(), port);
			try {
				final OChannelBinaryClient network = new OChannelBinaryClient(server.getKey(), port, clientConfiguration);

				OChannelBinaryProtocol.checkProtocolVersion(network);

				return network;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		final StringBuilder buffer = new StringBuilder();
		for (OPair<String, String[]> server : serverURLs) {
			if (buffer.length() > 0)
				buffer.append(',');
			buffer.append(server.getKey());
		}

		throw new OIOException("Can't connect to any configured remote nodes: " + buffer);
	}

	protected void checkConnection() {
		lock.acquireSharedLock();

		try {
			synchronized (networkPool) {

				if (networkPool.size() == 0)
					throw new ODatabaseException("Connection is closed");
			}

		} finally {
			lock.releaseSharedLock();
		}
	}

	static OIdentifiable readIdentifiable(final OChannelBinaryClient network, final ODatabaseRecord iDatabase) throws IOException {
		final int classId = network.readShort();
		if (classId == OChannelBinaryProtocol.RECORD_NULL)
			return null;

		if (classId == OChannelBinaryProtocol.RECORD_RID) {
			return network.readRID();
		} else {
			final ORecordInternal<?> record = ORecordFactory.newInstance(network.readByte());

			if (record instanceof ORecordSchemaAware<?>)
				((ORecordSchemaAware<?>) record).fill(iDatabase, classId, network.readRID(), network.readInt(), network.readBytes(), false);
			else
				// DISCARD CLASS ID
				record.fill(iDatabase, network.readRID(), network.readInt(), network.readBytes(), false);

			return record;
		}
	}

	/**
	 * Acquire a network channel from the pool. Don't lock the write stream since the connection usage is exclusive.
	 *
	 * @param iCommand
	 * @return
	 * @throws IOException
	 */
	protected OChannelBinaryClient beginRequest(final byte iCommand) throws IOException {
		OChannelBinaryClient network = null;

		if (debug)
			System.out.println("-> req: " + getSessionId());

		// FIND THE FIRST FREE CHANNEL AVAILABLE
		synchronized (networkPool) {
			final int beginCursor = networkPoolCursor;

			while (network == null) {
				network = networkPool.get(networkPoolCursor);
				if (network.getLockWrite().tryLock())
					break;

				network = null;

				networkPoolCursor++;

				if (networkPoolCursor >= networkPool.size())
					// RESTART FROM THE FIRST ONE
					networkPoolCursor = 0;

				if (networkPoolCursor == beginCursor) {
					// COMPLETE ROUND AND NOT FREE CONNECTIONS FOUND

					if (networkPool.size() < maxPool) {
						// CREATE NEW CONNECTION
						network = createNetworkConnection();
						network.getLockWrite().lock();
						networkPool.add(network);

						if (debug)
							System.out.println("Created new connection " + networkPool.size());
					} else {
						if (debug)
							System.out.println("-> req (waiting) : " + getSessionId());

						final long startToWait = System.currentTimeMillis();
						try {
							networkPool.wait(5000);
						} catch (InterruptedException e) {
							OProfiler.getInstance().updateCounter("network.connectionPool.timeout", +1);
						}

						final long elapsed = OProfiler.getInstance().stopChrono("network.connectionPool.waitingTime", startToWait);

						if (debug)
							System.out.println("Waiting for connection = elapsed: " + elapsed);
					}
				}
			}
		}

		network.writeByte(iCommand);
		network.writeInt(getSessionId());

		return network;
	}

	/**
	 * Ends the request and unlock the write lock
	 */
	public void endRequest(final OChannelBinaryClient iNetwork) throws IOException {
		if (iNetwork == null)
			return;

		try {
			iNetwork.flush();
		} catch (IOException e) {
			// IGNORE IT BECAUSE IT COULD BE CALLED AFTER A NETWORK ERROR TO RELEASE THE SOCKET
		}

		if (debug)
			System.out.println("<- req: " + getSessionId());

		iNetwork.getLockWrite().unlock();

		synchronized (networkPool) {
			networkPool.notifyAll();
		}
	}

	/**
	 * Starts listening the response.
	 */
	protected void beginResponse(final OChannelBinaryClient iNetwork) throws IOException {
		iNetwork.beginResponse(getSessionId());

		if (debug)
			System.out.println("-> res: " + getSessionId());
	}

	/**
	 * End response reached: release the channel in the pool to being reused
	 */
	public void endResponse(final OChannelBinaryClient iNetwork) {
		iNetwork.endResponse();

		if (debug)
			System.out.println("<- res: " + getSessionId());
	}

	public boolean isPermanentRequester() {
		return false;
	}

	protected void getResponse(final OChannelBinaryClient iNetwork) throws IOException {
		try {
			beginResponse(iNetwork);
		} finally {
			endResponse(iNetwork);
		}
	}

	public void updateClusterConfiguration(final byte[] iContent) {
		if (iContent == null)
			return;

		synchronized (OClientClusterManager.INSTANCE) {

			// GET DATABASE'S CLUSTER CONFIGURATION
			clusterConfiguration = OClientClusterManager.INSTANCE.get(name);
			if (clusterConfiguration == null) {
				clusterConfiguration = new ODocument();
				OClientClusterManager.INSTANCE.register(name, clusterConfiguration);
			}
		}

		synchronized (clusterConfiguration) {
			clusterConfiguration.reset();

			// UPDATE IT
			clusterConfiguration.fromStream(iContent);

			if (OLogManager.instance().isDebugEnabled())
				OLogManager.instance().debug(this, "Received new cluster configuration: %s", clusterConfiguration.toJSON(""));
		}
	}

	private void commitEntry(final OChannelBinaryClient iNetwork, final OTransactionRecordEntry txEntry) throws IOException {
		if (txEntry.status == OTransactionRecordEntry.LOADED)
			// JUMP LOADED OBJECTS
			return;

		iNetwork.writeByte((byte) 1);
		iNetwork.writeByte(txEntry.status);
		iNetwork.writeShort((short) txEntry.getRecord().getIdentity().getClusterId());
		iNetwork.writeLong(txEntry.getRecord().getIdentity().getClusterPosition());
		iNetwork.writeByte(txEntry.getRecord().getRecordType());

		switch (txEntry.status) {
		case OTransactionRecordEntry.CREATED:
			iNetwork.writeString(txEntry.clusterName);
			iNetwork.writeBytes(txEntry.getRecord().toStream());
			break;

		case OTransactionRecordEntry.UPDATED:
			iNetwork.writeInt(txEntry.getRecord().getVersion());
			iNetwork.writeBytes(txEntry.getRecord().toStream());
			break;

		case OTransactionRecordEntry.DELETED:
			iNetwork.writeInt(txEntry.getRecord().getVersion());
			break;
		}
	}

	protected void createConnectionPool() throws IOException, UnknownHostException {
		synchronized (networkPool) {

			// CREATE THE CHANNEL POOL
			if (networkPool.size() == 0) {
				// ALWAYS CREATE AT LEAST ONE CONNECTION
				final OChannelBinaryClient firstChannel = createNetworkConnection();
				networkPool.add(firstChannel);
				serviceThread = new OStorageRemoteServiceThread(new OStorageRemoteThread(this, Integer.MIN_VALUE), firstChannel);

				// CREATE THE MINIMUM POOL
				for (int i = 1; i < minPool; ++i)
					networkPool.add(createNetworkConnection());
			}

		}
	}
}