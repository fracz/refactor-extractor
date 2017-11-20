/*
 * Copyright 2010-2012 Luca Garulli (l.garulli--at--orientechnologies.com)
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
package com.orientechnologies.orient.server.distributed;

import java.util.List;
import java.util.concurrent.locks.Lock;

import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.server.distributed.ODistributedRequest.EXECUTION_MODE;
import com.orientechnologies.orient.server.distributed.conflict.OReplicationConflictResolver;
import com.orientechnologies.orient.server.distributed.task.OAbstractRemoteTask;
import com.orientechnologies.orient.server.distributed.task.OAbstractReplicatedTask;

/**
 * Server cluster interface to abstract cluster behavior.
 *
 * @author Luca Garulli (l.garulli--at--orientechnologies.com)
 *
 */
public interface ODistributedServerManager {

  public enum STATUS {
    STARTING, ONLINE, ALIGNING, SHUTDOWNING, OFFLINE
  };

  public boolean isEnabled();

  public STATUS getStatus();

  public boolean checkStatus(STATUS string);

  public void setStatus(STATUS iStatus);

  public boolean isOfflineNodeById(String iNodeName);

  public boolean isLocalNodeMaster(Object iKey);

  public String getLocalNodeId();

  public String getLocalNodeName();

  public ODocument getClusterConfiguration();

  public ODocument getNodeConfigurationById(String iNode);

  public ODocument getLocalNodeConfiguration();

  /**
   * Returns a time taking care about the offset with the cluster time. This allows to have a quite precise idea about information
   * on date times, such as logs to determine the youngest in case of conflict.
   *
   * @return
   */
  public long getDistributedTime(long iTme);

  public long getRunId();

  public long incrementDistributedSerial(String iDatabaseName);

  public OStorageSynchronizer getDatabaseSynchronizer(String iDatabaseName);

  void broadcastAlignmentRequest();

  /**
   * Communicates the alignment has been postponed. Current server will send an updated request of alignment against the postponed
   * node.
   */
  public void postponeAlignment(String iNode, String iDatabaseName);

  public void endAlignment(String nodeSource, String databaseName, int alignedOperations);

  /**
   * Gets a distributed lock
   *
   * @param iLockName
   *          name of the lock
   * @return
   */
  public Lock getLock(String iLockName);

  public Class<? extends OReplicationConflictResolver> getConfictResolverClass();

  public void updateJournal(String iDatabaseName, OAbstractReplicatedTask iTask, OStorageSynchronizer dbSynchronizer,
      long operationLogOffset, boolean iSuccess);

  public ODistributedConfiguration getDatabaseConfiguration(String iDatabaseName);

  public ODistributedPartition newPartition(List<String> partition);

  public ODistributedMessageService getMessageService();

  public Object sendRequest(String iDatabaseName, OAbstractRemoteTask iTask);

  public Object sendRequest(String iDatabaseName, OAbstractRemoteTask iTask, EXECUTION_MODE iExecutionMode);
}