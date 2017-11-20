package com.orientechnologies.orient.server.distributed.impl;

import com.orientechnologies.orient.core.db.ODatabaseDocumentInternal;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.tx.OTransaction;
import com.orientechnologies.orient.server.distributed.ODistributedRequest;
import com.orientechnologies.orient.server.distributed.ODistributedRequestId;
import com.orientechnologies.orient.server.distributed.ODistributedServerManager;
import com.orientechnologies.orient.server.distributed.ODistributedTxContext;
import com.orientechnologies.orient.server.distributed.task.ORemoteTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ONewDistributedTxContextImpl implements ODistributedTxContext {

  private final ODistributedDatabaseImpl shared;
  private final ODistributedRequestId    id;
  private final OTransaction             tx;
  private final long                     startedOn;
  private final List<ORID>   lockedRids = new ArrayList<>();
  private final List<Object> lockedKeys = new ArrayList<>();

  public ONewDistributedTxContextImpl(ODistributedDatabaseImpl shared, ODistributedRequestId reqId, OTransaction tx) {
    this.shared = shared;
    this.id = reqId;
    this.tx = tx;
    this.startedOn = System.currentTimeMillis();

  }

  @Override
  public void lockIndexKey(Object key) {
    shared.getIndexKeyLockManager().acquireExclusiveLock(key);
    lockedKeys.add(key);
  }

  @Override
  public void lock(ORID rid) {
    shared.getRecordLockManager().acquireExclusiveLock(rid);
    lockedRids.add(rid);
  }

  @Override
  public void lock(ORID rid, long timeout) {
    //TODO: the timeout is only in the lock manager, this implementation may need evolution
    shared.getRecordLockManager().acquireExclusiveLock(rid);
    lockedRids.add(rid);
  }

  @Override
  public void addUndoTask(ORemoteTask undoTask) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ODistributedRequestId getReqId() {
    return id;
  }

  @Override
  public void commit(ODatabaseDocumentInternal database) {
    database.rawBegin(tx);
    database.commit();
  }

  @Override
  public void fix(ODatabaseDocumentInternal database, List<ORemoteTask> fixTasks) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Set<ORecordId> rollback(ODatabaseDocumentInternal database) {
    return null;
  }

  @Override
  public void destroy() {
    unlock();
  }

  @Override
  public void clearUndo() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void unlock() {
    for (ORID lockedRid : lockedRids) {
      shared.getRecordLockManager().releaseExclusiveLock(lockedRid);
    }
    for (Object lockedKey : lockedKeys) {
      shared.getIndexKeyLockManager().releaseExclusiveLock(lockedKey);
    }
  }

  @Override
  public long getStartedOn() {
    return startedOn;
  }

  @Override
  public Set<ORecordId> cancel(ODistributedServerManager current, ODatabaseDocumentInternal database) {
    return null;
  }

  @Override
  public boolean isCanceled() {
    return false;
  }

}