/*
 *
 *  *  Copyright 2010-2016 OrientDB LTD (http://orientdb.com)
 *  *
 *  *  Licensed under the Apache License, Version 2.0 (the "License");
 *  *  you may not use this file except in compliance with the License.
 *  *  You may obtain a copy of the License at
 *  *
 *  *       http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *  Unless required by applicable law or agreed to in writing, software
 *  *  distributed under the License is distributed on an "AS IS" BASIS,
 *  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *  See the License for the specific language governing permissions and
 *  *  limitations under the License.
 *  *
 *  * For more information: http://orientdb.com
 *
 */

package com.orientechnologies.orient.core.db.document;

import com.orientechnologies.common.exception.OException;
import com.orientechnologies.orient.client.remote.OStorageRemote;
import com.orientechnologies.orient.client.remote.OStorageRemoteSession;
import com.orientechnologies.orient.client.remote.message.ORemoteResultSet;
import com.orientechnologies.orient.core.cache.OLocalRecordCache;
import com.orientechnologies.orient.core.config.OGlobalConfiguration;
import com.orientechnologies.orient.core.db.*;
import com.orientechnologies.orient.core.exception.ODatabaseException;
import com.orientechnologies.orient.core.hook.ORecordHook;
import com.orientechnologies.orient.core.index.ClassIndexManagerRemote;
import com.orientechnologies.orient.core.metadata.security.OImmutableUser;
import com.orientechnologies.orient.core.metadata.security.ORole;
import com.orientechnologies.orient.core.metadata.security.OToken;
import com.orientechnologies.orient.core.metadata.security.OUser;
import com.orientechnologies.orient.core.serialization.serializer.record.ORecordSerializerFactory;
import com.orientechnologies.orient.core.serialization.serializer.record.binary.ORecordSerializerNetwork;
import com.orientechnologies.orient.core.serialization.serializer.record.string.ORecordSerializerSchemaAware2CSV;
import com.orientechnologies.orient.core.sql.executor.OTodoResultSet;
import com.orientechnologies.orient.core.storage.OStorage;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Created by tglman on 30/06/16.
 */
public class ODatabaseDocumentRemote extends ODatabaseDocumentAbstract {

  protected OStorageRemoteSession sessionMetadata;
  private   OrientDBConfig        config;
  private   OStorageRemote        storage;

  public ODatabaseDocumentRemote(final OStorageRemote storage) {
    activateOnCurrentThread();

    try {
      status = STATUS.CLOSED;

      // OVERWRITE THE URL
      url = storage.getURL();
      this.storage = storage;
      this.componentsFactory = storage.getComponentsFactory();

      unmodifiableHooks = Collections.unmodifiableMap(hooks);

      localCache = new OLocalRecordCache();

      init();

      databaseOwner = this;
    } catch (Exception t) {
      ODatabaseRecordThreadLocal.INSTANCE.remove();

      throw OException.wrapException(new ODatabaseException("Error on opening database "), t);
    }

  }

  public <DB extends ODatabase> DB open(final String iUserName, final String iUserPassword) {
    throw new UnsupportedOperationException("Use OrientDBFactory");
  }

  @Deprecated public <DB extends ODatabase> DB open(final OToken iToken) {
    throw new UnsupportedOperationException("Deprecated Method");
  }

  @Override public <DB extends ODatabase> DB create() {
    throw new UnsupportedOperationException("Deprecated Method");
  }

  @Override public <DB extends ODatabase> DB create(String incrementalBackupPath) {
    throw new UnsupportedOperationException("use OrientDBFactory");
  }

  @Override public <DB extends ODatabase> DB create(final Map<OGlobalConfiguration, Object> iInitialSettings) {
    throw new UnsupportedOperationException("use OrientDBFactory");
  }

  @Override public void drop() {
    throw new UnsupportedOperationException("use OrientDBFactory");
  }

  public ODatabaseDocumentInternal copy() {
    ODatabaseDocumentRemote database = new ODatabaseDocumentRemote(storage);
    database.storage = storage.copy(this, database);
    database.storage.addUser();
    database.status = STATUS.OPEN;
    database.applyAttributes(config);
    database.initAtFirstOpen();
    database.user = this.user;
    this.activateOnCurrentThread();
    return database;
  }

  @Override public boolean exists() {
    throw new UnsupportedOperationException("use OrientDBFactory");
  }

  public void internalOpen(String user, String password, OrientDBConfig config) {
    this.config = config;
    boolean failure = true;
    applyAttributes(config);
    applyListeners(config);
    try {

      storage.open(user, password, config.getConfigurations());

      status = STATUS.OPEN;

      initAtFirstOpen();
      this.user = new OImmutableUser(-1, new OUser(user, OUser.encryptPassword(password))
          .addRole(new ORole("passthrough", null, ORole.ALLOW_MODES.ALLOW_ALL_BUT)));

      // WAKE UP LISTENERS
      callOnOpenListeners();

      failure = false;
    } catch (OException e) {
      close();
      throw e;
    } catch (Exception e) {
      close();
      throw OException.wrapException(new ODatabaseException("Cannot open database url=" + getURL()), e);
    }
  }

  private void applyAttributes(OrientDBConfig config) {
    for (Entry<ATTRIBUTES, Object> attrs : config.getAttributes().entrySet()) {
      this.set(attrs.getKey(), attrs.getValue());
    }
  }

  private void initAtFirstOpen() {
    if (initialized)
      return;

    ORecordSerializerFactory serializerFactory = ORecordSerializerFactory.instance();
    serializer = serializerFactory.getFormat(ORecordSerializerNetwork.NAME);
    localCache.startup();
    componentsFactory = getStorage().getComponentsFactory();
    user = null;

    loadMetadata();
    installHooksRemote();

    initialized = true;
  }

  protected void installHooksRemote() {
    hooks.clear();
    registerHook(new ClassIndexManagerRemote(this), ORecordHook.HOOK_POSITION.LAST);
  }

  private void applyListeners(OrientDBConfig config) {
    for (ODatabaseListener listener : config.getListeners()) {
      registerListener(listener);
    }
  }

  public OStorageRemoteSession getSessionMetadata() {
    return sessionMetadata;
  }

  public void setSessionMetadata(OStorageRemoteSession sessionMetadata) {
    this.sessionMetadata = sessionMetadata;
  }

  @Override public OStorage getStorage() {
    return storage;
  }

  @Override public void replaceStorage(OStorage iNewStorage) {
    throw new UnsupportedOperationException("unsupported replace of storage for remote database");
  }

  @Override public OTodoResultSet query(String query, Object[] args) {
    return storage.query(this, query, args);
  }

  @Override public OTodoResultSet query(String query, Map args) {
    return storage.query(this, query, args);
  }

  @Override public OTodoResultSet command(String query, Object[] args) {
    return storage.command(this, query, args);
  }

  @Override public OTodoResultSet command(String query, Map args) {
    return storage.command(this, query, args);
  }

  public void closeQuery(String queryId) {
    storage.closeQuery(this, queryId);
  }

  public void fetchNextPage(ORemoteResultSet rs) {
    storage.fetchNextPage(this, rs);
  }
}