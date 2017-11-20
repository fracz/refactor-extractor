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
package com.orientechnologies.orient.server.distributed.task;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;

import com.orientechnologies.orient.server.OServer;
import com.orientechnologies.orient.server.distributed.ODistributedServerLog;
import com.orientechnologies.orient.server.distributed.ODistributedServerLog.DIRECTION;
import com.orientechnologies.orient.server.distributed.ODistributedServerManager;
import com.orientechnologies.orient.server.distributed.ODistributedServerManager.EXECUTION_MODE;
import com.orientechnologies.orient.server.distributed.OStorageSynchronizer;
import com.orientechnologies.orient.server.journal.ODatabaseJournal;
import com.orientechnologies.orient.server.journal.ODatabaseJournal.OPERATION_TYPES;

/**
 * Distributed align request task used for synchronization.
 *
 * @author Luca Garulli (l.garulli--at--orientechnologies.com)
 *
 */
public class OAlignRequestTask extends OAbstractReplicatedTask<Integer> {
  private static final int   MAX_TIMEOUT      = 60000;

  private static final long  serialVersionUID = 1L;

  protected long             lastRunId;
  protected long             lastOperationId;
  protected static final int OP_BUFFER        = 150;

  public OAlignRequestTask() {
  }

  public OAlignRequestTask(final OServer iServer, final ODistributedServerManager iDistributedSrvMgr, final String iDbName,
      final long iLastRunId, final long iLastOperationId) {
    super(iServer, iDistributedSrvMgr, iDbName, -1);
    lastRunId = iLastRunId;
    lastOperationId = iLastOperationId;
  }

  @Override
  public Integer call() throws Exception {
    if (lastRunId == -1 && lastOperationId == -1)
      ODistributedServerLog.info(this, getDistributedServerManager().getLocalNodeName(), getNodeSource(), DIRECTION.IN,
          "db=%s align request starting from the beginning (no log found)", databaseName);
    else
      ODistributedServerLog.info(this, getDistributedServerManager().getLocalNodeName(), getNodeSource(), DIRECTION.IN,
          "db=%s align request starting from operation %d.%d", databaseName, lastRunId, lastOperationId);

    int totAligned;

    final ODistributedServerManager dManager = getDistributedServerManager();

    final String localNode = dManager.getLocalNodeName();

    final OStorageSynchronizer synchronizer = getDatabaseSynchronizer();
    if (synchronizer == null)
      return 0;

    final ODatabaseJournal log = synchronizer.getLog();

    // GET THE DISTRIBUTED LOCK TO ALIGN THE DATABASE
    final Lock alignmentLock = dManager.getLock("align." + databaseName);
    if (alignmentLock.tryLock())
      try {
        totAligned = 0;
        int aligned = 0;

        ODistributedServerLog.warn(this, getDistributedServerManager().getLocalNodeName(), getNodeSource(), DIRECTION.OUT,
            "****** BEGIN PREPARING ALIGNMENT BLOCK db=%s ******", databaseName);

        final OMultipleRemoteTasks tasks = new OMultipleRemoteTasks(serverInstance, dManager, databaseName,
            EXECUTION_MODE.SYNCHRONOUS);
        final List<Long> positions = new ArrayList<Long>();

        final Iterator<Long> it = log.browseLastOperations(new long[] { lastRunId, lastOperationId },
            ODatabaseJournal.OPERATION_STATUS.COMMITTED, -1);
        while (it.hasNext()) {
          final long pos = it.next();

          final OAbstractReplicatedTask<?> operation = log.getOperation(pos);
          if (operation == null) {
            ODistributedServerLog.info(this, getDistributedServerManager().getLocalNodeName(), getNodeSource(), DIRECTION.OUT,
                "#%d db=%s skipped operation", aligned, databaseName);
            continue;
          }

          ODistributedServerLog.info(this, getDistributedServerManager().getLocalNodeName(), getNodeSource(), DIRECTION.OUT,
              "#%d aligning operation=%d.%d db=%s %s", aligned, operation.getRunId(), operation.getOperationSerial(), databaseName,
              operation);

          operation.setNodeSource(localNode);
          operation.setDatabaseName(databaseName);

          tasks.addTask(operation);
          positions.add(pos);

          aligned++;

          if (tasks.getTasks() >= OP_BUFFER)
            totAligned += flushBufferedTasks(dManager, synchronizer, tasks, positions);
        }

        if (tasks.getTasks() > 0)
          totAligned += flushBufferedTasks(dManager, synchronizer, tasks, positions);

        ODistributedServerLog.warn(this, getDistributedServerManager().getLocalNodeName(), getNodeSource(), DIRECTION.OUT,
            "****** END PREPARING ALIGNMENT BLOCK db=%s total=%d ******", databaseName, totAligned);

      } finally {
        alignmentLock.unlock();
      }
    else
      // SEND BACK -1 TO RESEND THE UPDATED ALIGNMENT REQUEST
      totAligned = -1;

    // SEND TO THE REQUESTER NODE THE TASK TO EXECUTE
    final Future<Object> future = dManager.sendTask2Node(getNodeSource(), new OAlignResponseTask(serverInstance, dManager,
        databaseName, totAligned), EXECUTION_MODE.ASYNCHRONOUS, new HashMap<String, Object>());

    if (future != null) {
      // WAIT FOR THE TASK IS COMPLETED
      try {
        future.get(getTimeout(), TimeUnit.MILLISECONDS);
      } catch (TimeoutException e) {
        // TIMEOUT
        ODistributedServerLog.error(this, getDistributedServerManager().getLocalNodeName(), getNodeSource(), DIRECTION.OUT,
            "timeout on alignment request db=%s...", databaseName);
        future.cancel(true);
      }
    }

    return totAligned;
  }

  protected int flushBufferedTasks(final ODistributedServerManager dManager, final OStorageSynchronizer synchronizer,
      final OMultipleRemoteTasks tasks, final List<Long> positions) throws IOException, InterruptedException, ExecutionException {

    ODistributedServerLog.info(this, getDistributedServerManager().getLocalNodeName(), getNodeSource(), DIRECTION.OUT,
        "flushing aligning %d operations db=%s...", tasks.getTasks(), databaseName);

    // SEND TO THE REQUESTER NODE THE TASK TO EXECUTE
    final Future<Object> future = dManager.sendTask2Node(getNodeSource(), tasks, EXECUTION_MODE.SYNCHRONOUS,
        new HashMap<String, Object>());

    if (future != null) {
      try {
        future.get(getTimeout(), TimeUnit.MILLISECONDS);
      } catch (TimeoutException e) {
        // TIMEOUT
        ODistributedServerLog.error(this, getDistributedServerManager().getLocalNodeName(), getNodeSource(), DIRECTION.OUT,
            "timeout on flushing alignment operations db=%s...", databaseName);
        future.cancel(true);
      }
    }

    final int aligned = tasks.getTasks();

    ODistributedServerLog.info(this, getDistributedServerManager().getLocalNodeName(), getNodeSource(), DIRECTION.OUT,
        " flushed aligning %d operations db=%s...", aligned, databaseName);

    // REUSE THE MULTIPLE TASK
    tasks.clearTasks();
    positions.clear();

    return aligned;
  }

  @Override
  public void writeExternal(final ObjectOutput out) throws IOException {
    super.writeExternal(out);
    out.writeLong(lastRunId);
    out.writeLong(lastOperationId);
  }

  @Override
  public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
    super.readExternal(in);
    lastRunId = in.readLong();
    lastOperationId = in.readLong();
  }

  @Override
  public String getName() {
    return "align_request";
  }

  @Override
  public OAlignRequestTask copy() {
    final OAlignRequestTask copy = (OAlignRequestTask) super.copy(new OAlignRequestTask());
    copy.lastRunId = lastRunId;
    copy.lastOperationId = lastOperationId;
    return copy;
  }

  @Override
  public OPERATION_TYPES getOperationType() {
    return OPERATION_TYPES.NOOP;
  }

  @Override
  public String getPayload() {
    return lastRunId + "." + lastOperationId;
  }

  @Override
  public long getTimeout() {
    return MAX_TIMEOUT;
  }
}