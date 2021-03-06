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
import com.orientechnologies.common.util.OPair;
import com.orientechnologies.orient.client.dictionary.ODictionaryClient;
import com.orientechnologies.orient.client.distributed.OClientClusterManager;
import com.orientechnologies.orient.core.Orient;
import com.orientechnologies.orient.core.command.OCommandRequestAsynch;
import com.orientechnologies.orient.core.command.OCommandRequestText;
import com.orientechnologies.orient.core.config.OContextConfiguration;
import com.orientechnologies.orient.core.config.OGlobalConfiguration;
import com.orientechnologies.orient.core.config.OStorageConfiguration;
import com.orientechnologies.orient.core.db.record.ODatabaseRecord;
import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.dictionary.ODictionary;
import com.orientechnologies.orient.core.exception.OCommandExecutionException;
import com.orientechnologies.orient.core.exception.ODatabaseException;
import com.orientechnologies.orient.core.exception.OStorageException;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.id.ORecordId;
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
import com.orientechnologies.orient.core.tx.OTransactionEntry;
import com.orientechnologies.orient.core.tx.OTransactionRealAbstract;
import com.orientechnologies.orient.enterprise.channel.binary.OChannelBinaryClient;
import com.orientechnologies.orient.enterprise.channel.binary.OChannelBinaryProtocol;

/**
 * This object is bound to each remote ODatabase instances.
 */
@SuppressWarnings("unchecked")
public class OStorageRemote extends OStorageAbstract {
	private static final String								DEFAULT_HOST			= "localhost";
	private static final String[]							DEFAULT_PORTS			= new String[] { "2424" };
	private static final String								ADDRESS_SEPARATOR	= ";";

	private OStorageRemoteServiceThread				serviceThread;
	private OContextConfiguration							clientConfiguration;
	private int																connectionRetry;
	private int																connectionRetryDelay;

	private static List<OChannelBinaryClient>	networkPool				= new ArrayList<OChannelBinaryClient>();
	protected List<OPair<String, String[]>>		serverURLs				= new ArrayList<OPair<String, String[]>>();
	protected int															retry							= 0;
	protected final Map<String, Integer>			clustersIds				= new HashMap<String, Integer>();
	protected final Map<String, String>				clustersTypes			= new HashMap<String, String>();
	protected int															defaultClusterId;
	private int																networkPoolCursor	= 0;

	public OStorageRemote(final String iURL, final String iMode) throws IOException {
		super(iURL, iURL, iMode);
		configuration = null;

		clientConfiguration = new OContextConfiguration();
		connectionRetry = clientConfiguration.getValueAsInteger(OGlobalConfiguration.NETWORK_SOCKET_RETRY);
		connectionRetryDelay = clientConfiguration.getValueAsInteger(OGlobalConfiguration.NETWORK_SOCKET_RETRY_DELAY);

		level2cache.startup();

		parseServerURLs();
	}

	public int getSessionId() {
		return OStorageRemoteThreadLocal.INSTANCE.get().sessionId.intValue();
	}

	public void setSessionId(final int iSessionId) {
		OStorageRemoteThreadLocal.INSTANCE.get().sessionId = iSessionId;
	}

	public void open(final String iUserName, final String iUserPassword, final Map<String, Object> iOptions) {
		addUser();

		final boolean locked = lock.acquireExclusiveLock();

		try {
			openRemoteDatabase(iUserName, iUserPassword);

			configuration = new OStorageConfiguration(this);
			configuration.load();

		} catch (Exception e) {
			if (!OGlobalConfiguration.STORAGE_KEEP_OPEN.getValueAsBoolean())
				close();

			if (e instanceof OException)
				throw (OException) e;
			else
				throw new OStorageException("Can't open the remote storage: " + name, e);

		} finally {
			lock.releaseExclusiveLock(locked);
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
		final boolean locked = lock.acquireExclusiveLock();

		OChannelBinaryClient network = null;
		try {

			network = beginRequest(OChannelBinaryProtocol.REQUEST_DB_CLOSE);
			endRequest(network);

			getResponse(network);

			OStorageRemoteThreadLocal.INSTANCE.get().sessionId = -1;

			if (!checkForClose(iForce))
				return;

			// CLOSE THE CHANNEL
			if (serviceThread != null) {
				serviceThread.sendShutdown();
				serviceThread.interrupt();
			}

			for (OChannelBinaryClient n : networkPool) {
				n.close();
			}
			networkPool.clear();

			level2cache.shutdown();

			Orient.instance().unregisterStorage(this);
			open = false;

		} catch (Exception e) {
			OLogManager.instance().debug(this, "Error on closing remote connection: %s", network);
		} finally {

			lock.releaseExclusiveLock(locked);
		}
	}

	public void delete() {
		throw new UnsupportedOperationException(
				"Can't delete a database in a remote server. Please use the console or the OServerAdmin class.");
	}

	public Set<String> getClusterNames() {
		final boolean locked = lock.acquireSharedLock();

		try {
			checkConnection();
			return clustersIds.keySet();

		} finally {
			lock.releaseSharedLock(locked);
		}
	}

	public long createRecord(final ORecordId iRid, final byte[] iContent, final byte iRecordType) {
		checkConnection();

		do {
			try {

				final OChannelBinaryClient network = beginRequest(OChannelBinaryProtocol.REQUEST_RECORD_CREATE);
				try {
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

				final OChannelBinaryClient network = beginRequest(OChannelBinaryProtocol.REQUEST_RECORD_LOAD);
				try {
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
						record = (ORecordInternal<?>) readRecordFromNetwork(network, iDatabase);

						// PUT IN THE CLIENT LOCAL CACHE
						level2cache.pushRecord(record);
					}
					return buffer;
				} finally {
					endResponse(network);
				}

			} catch (Exception e) {
				handleException("Error on read record #" + iRid, e);

			}
		} while (true);
	}

	public int updateRecord(final ORecordId iRid, final byte[] iContent, final int iVersion, final byte iRecordType) {
		checkConnection();

		do {
			try {

				final OChannelBinaryClient network = beginRequest(OChannelBinaryProtocol.REQUEST_RECORD_UPDATE);
				try {
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

			} catch (Exception e) {
				handleException("Error on update record #" + iRid, e);

			}
		} while (true);
	}

	public boolean deleteRecord(final ORecordId iRid, final int iVersion) {
		checkConnection();

		do {
			try {

				final OChannelBinaryClient network = beginRequest(OChannelBinaryProtocol.REQUEST_RECORD_DELETE);
				try {
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

				final OChannelBinaryClient network = beginRequest(OChannelBinaryProtocol.REQUEST_DATACLUSTER_DATARANGE);
				try {
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

			} catch (Exception e) {
				handleException("Error on getting last entry position count in cluster: " + iClusterId, e);

			}
		} while (true);
	}

	public long getSize() {
		checkConnection();

		do {
			try {

				final OChannelBinaryClient network = beginRequest(OChannelBinaryProtocol.REQUEST_DB_SIZE);
				endRequest(network);

				try {
					beginResponse(network);
					return network.readLong();
				} finally {
					endResponse(network);
				}
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

				final OChannelBinaryClient network = beginRequest(OChannelBinaryProtocol.REQUEST_DB_COUNTRECORDS);
				endRequest(network);

				try {
					beginResponse(network);
					return network.readLong();
				} finally {
					endResponse(network);
				}
			} catch (Exception e) {
				handleException("Error on read database record count", e);

			}
		} while (true);
	}

	public long count(final int[] iClusterIds) {
		checkConnection();

		do {
			try {

				final OChannelBinaryClient network = beginRequest(OChannelBinaryProtocol.REQUEST_DATACLUSTER_COUNT);
				try {
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
			} catch (Exception e) {
				handleException("Error on read record count in clusters: " + iClusterIds, e);

			}
		} while (true);
	}

	public long count(final String iClassName) {
		checkConnection();

		do {

			try {

				final OChannelBinaryClient network = beginRequest(OChannelBinaryProtocol.REQUEST_COUNT);
				try {
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

				final OChannelBinaryClient network = beginRequest(OChannelBinaryProtocol.REQUEST_COMMAND);
				try {
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
							ORecordSchemaAware<?> record = (ORecordSchemaAware<?>) readRecordFromNetwork(network, iCommand.getDatabase());
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
								break;

							case 2:
								// PUT IN THE CLIENT LOCAL CACHE
								level2cache.pushRecord(record);
							}
						}
					} else {
						final byte type = network.readByte();
						switch (type) {
						case 'n':
							result = null;
							break;

						case 'r':
							result = readRecordFromNetwork(network, iCommand.getDatabase());
							break;

						case 'l':
							final int tot = network.readInt();
							final Collection<OIdentifiable> list = new ArrayList<OIdentifiable>();
							for (int i = 0; i < tot; ++i) {
								list.add(readRecordFromNetwork(network, iCommand.getDatabase()));
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

				final Set<OTransactionEntry> allEntries = new HashSet<OTransactionEntry>();

				final OChannelBinaryClient network = beginRequest(OChannelBinaryProtocol.REQUEST_TX_COMMIT);
				try {
					network.writeInt(((OTransactionRealAbstract) iTx).getId());

					final List<OTransactionEntry> tmpEntries = new ArrayList<OTransactionEntry>();

					while (iTx.getEntries().iterator().hasNext()) {
						for (OTransactionEntry txEntry : iTx.getEntries())
							if (!allEntries.contains(txEntry))
								tmpEntries.add(txEntry);

						iTx.clearEntries();

						if (tmpEntries.size() > 0) {
							for (OTransactionEntry txEntry : tmpEntries)
								commitEntry(network, txEntry);

							allEntries.addAll(tmpEntries);
							tmpEntries.clear();
						}
					}

					network.writeByte((byte) 0);

				} finally {
					endRequest(network);
				}

				try {
					beginResponse(network);
					final int updatedRecords = network.readInt();
					ORecordId rid;
					for (int i = 0; i < updatedRecords; ++i) {
						rid = network.readRID();

						// SEARCH THE RECORD WITH THAT ID TO UPDATE THE VERSION
						for (OTransactionEntry txEntry : allEntries) {
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
			} catch (Exception e) {
				handleException("Error on commit", e);

			}
		} while (true);
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

				final OChannelBinaryClient network = beginRequest(OChannelBinaryProtocol.REQUEST_DATACLUSTER_ADD);
				try {
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
			} catch (Exception e) {
				handleException("Error on add new cluster", e);

			}
		} while (true);
	}

	public boolean removeCluster(final int iClusterId) {
		checkConnection();

		do {

			try {

				final OChannelBinaryClient network = beginRequest(OChannelBinaryProtocol.REQUEST_DATACLUSTER_REMOVE);
				try {
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
								break;
							}

						return true;
					}
					return false;
				} finally {
					endResponse(network);
				}

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

				final OChannelBinaryClient network = beginRequest(OChannelBinaryProtocol.REQUEST_DATASEGMENT_ADD);
				try {
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

			} catch (Exception e) {
				handleException("Error on add new data segment", e);

			}
		} while (true);
	}

	public <REC extends ORecordInternal<?>> REC dictionaryPut(final ODatabaseRecord iDatabase, final String iKey,
			final ORecordInternal<?> iRecord) {
		checkConnection();

		do {

			try {
				final ORID rid = iRecord.getIdentity();

				final OChannelBinaryClient network = beginRequest(OChannelBinaryProtocol.REQUEST_INDEX_PUT);
				try {
					network.writeString(iKey);
					network.writeByte(iRecord.getRecordType());
					network.writeRID(rid);
				} finally {
					endRequest(network);
				}

				try {
					beginResponse(network);
					return (REC) readRecordFromNetwork(network, iDatabase);
				} finally {
					endResponse(network);
				}

			} catch (Exception e) {
				handleException("Error on insert record with key: " + iKey, e);

			}
		} while (true);
	}

	public <REC extends ORecordInternal<?>> REC dictionaryLookup(ODatabaseRecord iDatabase, final String iKey) {
		checkConnection();

		do {

			try {

				final OChannelBinaryClient network = beginRequest(OChannelBinaryProtocol.REQUEST_INDEX_LOOKUP);
				try {
					network.writeString(iKey);
				} finally {
					endRequest(network);
				}

				try {
					beginResponse(network);
					return (REC) readRecordFromNetwork(network, iDatabase);
				} finally {
					endResponse(network);
				}

			} catch (Exception e) {
				handleException("Error on lookup record with key: " + iKey, e);

			}
		} while (true);
	}

	public <REC extends ORecordInternal<?>> REC dictionaryRemove(ODatabaseRecord iDatabase, Object iKey) {
		checkConnection();

		do {
			try {

				final OChannelBinaryClient network = beginRequest(OChannelBinaryProtocol.REQUEST_INDEX_REMOVE);
				try {
					network.writeString(iKey.toString());
				} finally {
					endRequest(network);
				}

				try {
					beginResponse(network);
					return (REC) readRecordFromNetwork(network, iDatabase);
				} finally {
					endResponse(network);
				}

			} catch (Exception e) {
				handleException("Error on lookup record with key: " + iKey, e);

			}
		} while (true);
	}

	public int dictionarySize(final ODatabaseRecord iDatabase) {
		checkConnection();

		do {

			try {

				final OChannelBinaryClient network = beginRequest(OChannelBinaryProtocol.REQUEST_INDEX_SIZE);
				endRequest(network);

				try {
					beginResponse(network);
					return network.readInt();
				} finally {
					endResponse(network);
				}

			} catch (Exception e) {
				handleException("Error on getting size of database's dictionary", e);

			}
		} while (true);
	}

	public ODictionary<?> createDictionary(final ODatabaseRecord iDatabase) throws Exception {
		return new ODictionaryClient<Object>(iDatabase, this);
	}

	public Set<String> dictionaryKeys(final ODatabaseRecord iDatabase) {
		checkConnection();

		do {
			try {

				final OChannelBinaryClient network = beginRequest(OChannelBinaryProtocol.REQUEST_INDEX_KEYS);
				endRequest(network);

				try {
					beginResponse(network);
					return network.readStringSet();
				} finally {
					endResponse(network);
				}

			} catch (Exception e) {
				handleException("Error on getting keys of database's dictionary", e);

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

				openRemoteDatabase(null, null);

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

	protected void openRemoteDatabase(final String iUserName, final String iUserPassword) throws IOException {
		createConnectionPool();

		final OChannelBinaryClient network = beginRequest(OChannelBinaryProtocol.REQUEST_DB_OPEN);
		try {
			network.writeString(name).writeString(iUserName).writeString(iUserPassword);
		} finally {
			endRequest(network);
		}

		try {
			beginResponse(network);

			final int sessionId = network.readInt();
			setSessionId(sessionId);

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
		if (networkPool.size() == 0)
			throw new ODatabaseException("Connection is closed");
	}

	private OIdentifiable readRecordFromNetwork(final OChannelBinaryClient network, final ODatabaseRecord iDatabase)
			throws IOException {
		final int classId = network.readShort();
		if (classId == OChannelBinaryProtocol.RECORD_NULL)
			return null;

		if (classId == OChannelBinaryProtocol.RECORD_RID) {
			return new ORecordId(network.readShort(), network.readLong());
		} else {
			final ORecordInternal<?> record = ORecordFactory.newInstance(network.readByte());

			if (record instanceof ORecordSchemaAware<?>)
				((ORecordSchemaAware<?>) record).fill(iDatabase, classId, network.readRID(), network.readInt(), network.readBytes());
			else
				// DISCARD CLASS ID
				record.fill(iDatabase, network.readRID(), network.readInt(), network.readBytes());

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

		// FIND THE FIRST FREE CHANNEL AVAILABLE
		synchronized (networkPool) {
			while (network == null) {
				if (networkPoolCursor >= networkPool.size())
					networkPoolCursor = 0;

				network = networkPool.get(networkPoolCursor);
				if (network.getLockWrite().tryLock())
					break;

				networkPoolCursor++;
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
		iNetwork.flush();
	}

	/**
	 * Starts listening the response.
	 */
	protected void beginResponse(final OChannelBinaryClient iNetwork) throws IOException {
		iNetwork.beginResponse(getSessionId());
	}

	/**
	 * End response reached: release the channel in the pool to being reused
	 */
	public void endResponse(final OChannelBinaryClient iNetwork) {
		iNetwork.endResponse();
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

		ODocument clusterConfiguration;

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

			if (OLogManager.instance().isInfoEnabled())
				OLogManager.instance().info(this, "Received new cluster configuration: %s", clusterConfiguration.toJSON(""));
		}
	}

	private void commitEntry(final OChannelBinaryClient iNetwork, final OTransactionEntry txEntry) throws IOException {
		if (txEntry.status == OTransactionEntry.LOADED)
			// JUMP LOADED OBJECTS
			return;

		iNetwork.writeByte((byte) 1);
		iNetwork.writeByte(txEntry.status);
		iNetwork.writeShort((short) txEntry.getRecord().getIdentity().getClusterId());
		iNetwork.writeLong(txEntry.getRecord().getIdentity().getClusterPosition());
		iNetwork.writeByte(txEntry.getRecord().getRecordType());

		switch (txEntry.status) {
		case OTransactionEntry.CREATED:
			iNetwork.writeString(txEntry.clusterName);
			iNetwork.writeBytes(txEntry.getRecord().toStream());
			break;

		case OTransactionEntry.UPDATED:
			iNetwork.writeInt(txEntry.getRecord().getVersion());
			iNetwork.writeBytes(txEntry.getRecord().toStream());
			break;

		case OTransactionEntry.DELETED:
			iNetwork.writeInt(txEntry.getRecord().getVersion());
			break;
		}
	}

	protected void createConnectionPool() throws IOException, UnknownHostException {
		if (networkPool.size() == 0) {
			// CONNECT TO THE SERVER
			final OChannelBinaryClient firstChannel = createNetworkConnection();
			networkPool.add(firstChannel);

			serviceThread = new OStorageRemoteServiceThread(new OStorageRemoteThread(this, OStorageRemoteServiceThread.REQ_ID),
					firstChannel);
		}
	}
}