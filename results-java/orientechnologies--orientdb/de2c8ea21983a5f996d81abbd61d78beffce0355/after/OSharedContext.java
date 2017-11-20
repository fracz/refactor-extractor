package com.orientechnologies.orient.core.db;

import com.orientechnologies.common.concur.resource.OCloseable;
import com.orientechnologies.orient.core.cache.OCommandCache;
import com.orientechnologies.orient.core.cache.OCommandCacheSoftRefs;
import com.orientechnologies.orient.core.config.OGlobalConfiguration;
import com.orientechnologies.orient.core.index.*;
import com.orientechnologies.orient.core.metadata.function.OFunctionLibraryImpl;
import com.orientechnologies.orient.core.metadata.schema.OSchemaShared;
import com.orientechnologies.orient.core.metadata.security.OSecurity;
import com.orientechnologies.orient.core.metadata.sequence.OSequenceLibraryImpl;
import com.orientechnologies.orient.core.query.live.OLiveQueryHook;
import com.orientechnologies.orient.core.schedule.OSchedulerImpl;
import com.orientechnologies.orient.core.security.OSecurityManager;
import com.orientechnologies.orient.core.sql.parser.OStatementCache;
import com.orientechnologies.orient.core.storage.OStorageProxy;

import java.io.Closeable;

/**
 * Created by tglman on 15/06/16.
 */
public class OSharedContext implements OCloseable {

  private OSchemaShared                schema;
  private OSecurity                    security;
  private OIndexManagerAbstract        indexManager;
  private OFunctionLibraryImpl         functionLibrary;
  private OSchedulerImpl               scheduler;
  private OSequenceLibraryImpl         sequenceLibrary;
  private OLiveQueryHook.OLiveQueryOps liveQueryOps;
  private OCommandCache                commandCache;
  private OStatementCache              statementCache;

  private volatile boolean loaded = false;

  public OSharedContext(ODatabaseDocumentInternal database) {
    schema = new OSchemaShared(database.getStorageVersions().classesAreDetectedByClusterId());

    security = OSecurityManager.instance().newSecurity();

    if (database.getStorage() instanceof OStorageProxy)
      indexManager = new OIndexManagerRemote(database);
    else
      indexManager = new OIndexManagerShared(database);

    functionLibrary = new OFunctionLibraryImpl();
    scheduler = new OSchedulerImpl();
    sequenceLibrary = new OSequenceLibraryImpl();
    liveQueryOps = new OLiveQueryHook.OLiveQueryOps();
    commandCache = new OCommandCacheSoftRefs(database.getName());
    statementCache = new OStatementCache(OGlobalConfiguration.STATEMENT_CACHE_SIZE.getValueAsInteger());
  }


  public synchronized void load(ODatabaseDocumentInternal database) {
    if (!loaded) {
      schema.load();
      security.load();
      indexManager.load();
      if (!(database.getStorage() instanceof OStorageProxy)) {
        functionLibrary.load();
        scheduler.load();
      }
      sequenceLibrary.load();
      schema.onPostIndexManagement();
      loaded = true;
    }
  }

  @Override
  public synchronized void close() {
    schema.close();
    security.close(false);
    indexManager.close();
    functionLibrary.close();
    scheduler.close();
    sequenceLibrary.close();
    if (commandCache != null) {
      commandCache.clear();
      commandCache.shutdown();
    }
    if (liveQueryOps != null)
      liveQueryOps.close();
  }

  public synchronized void reload() {
    schema.reload();
    indexManager.reload();
    security.load();
    functionLibrary.load();
    sequenceLibrary.load();
    commandCache.clear();
    scheduler.load();
  }

  public synchronized void create() {
    schema.create();
    indexManager.create();
    security.create();
    functionLibrary.create();
    sequenceLibrary.create();
    security.createClassTrigger();
    scheduler.create();

    // CREATE BASE VERTEX AND EDGE CLASSES
    schema.createClass("V");
    schema.createClass("E");
    loaded = true;
  }

  public OSchemaShared getSchema() {
    return schema;
  }

  public OSecurity getSecurity() {
    return security;
  }

  public OIndexManagerAbstract getIndexManager() {
    return indexManager;
  }

  public OFunctionLibraryImpl getFunctionLibrary() {
    return functionLibrary;
  }

  public OSchedulerImpl getScheduler() {
    return scheduler;
  }

  public OSequenceLibraryImpl getSequenceLibrary() {
    return sequenceLibrary;
  }

  public OLiveQueryHook.OLiveQueryOps getLiveQueryOps() {
    return liveQueryOps;
  }

  public OCommandCache getCommandCache() {
    return commandCache;
  }

  public OStatementCache getStatementCache() {
    return statementCache;
  }


}