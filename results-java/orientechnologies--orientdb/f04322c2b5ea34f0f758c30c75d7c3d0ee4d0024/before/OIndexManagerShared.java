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
package com.orientechnologies.orient.core.index;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.orientechnologies.common.listener.OProgressListener;
import com.orientechnologies.common.log.OLogManager;
import com.orientechnologies.common.util.OMultiKey;
import com.orientechnologies.orient.core.db.ODatabase;
import com.orientechnologies.orient.core.db.ODatabaseRecordThreadLocal;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.db.record.ODatabaseRecord;
import com.orientechnologies.orient.core.db.record.ORecordElement;
import com.orientechnologies.orient.core.db.record.ORecordTrackedSet;
import com.orientechnologies.orient.core.metadata.OMetadata;
import com.orientechnologies.orient.core.metadata.schema.OSchemaShared;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.storage.OCluster;
import com.orientechnologies.orient.core.storage.OCluster.ATTRIBUTES;
import com.orientechnologies.orient.core.storage.impl.local.OClusterLocal;

/**
 * Manages indexes at database level. A single instance is shared among multiple databases. Contentions are managed by r/w locks.
 *
 * @author Luca Garulli (l.garulli--at--orientechnologies.com)
 * @author Artem Orobets added composite index managemement
 *
 */
public class OIndexManagerShared extends OIndexManagerAbstract implements OIndexManager {
  private static final long   serialVersionUID     = 1L;

  protected volatile Runnable rebuildIndexesThread = null;

  public OIndexManagerShared(final ODatabaseRecord iDatabase) {
    super(iDatabase);
  }

  public OIndex<?> getIndexInternal(final String iName) {
    acquireSharedLock();
    try {
      final OIndex<?> index = indexes.get(iName.toLowerCase());
      return getIndexInstance(index);
    } finally {
      releaseSharedLock();
    }
  }

  /**
   *
   *
   * @param iName
   *          - name of index
   * @param iType
   * @param iClusterIdsToIndex
   * @param iProgressListener
   */
  public OIndex<?> createIndex(final String iName, final String iType, final OIndexDefinition indexDefinition,
      final int[] iClusterIdsToIndex, final OProgressListener iProgressListener) {
    if (getDatabase().getTransaction().isActive())
      throw new IllegalStateException("Cannot create a new index inside a transaction");

    final Character c = OSchemaShared.checkNameIfValid(iName);
    if (c != null)
      throw new IllegalArgumentException("Invalid index name '" + iName + "'. Character '" + c + "' is invalid");

    acquireExclusiveLock();
    try {
      final OIndexInternal<?> index = OIndexes.createIndex(getDatabase(), iType);

      // decide which cluster to use ("index" - for automatic and "manindex" for manual)
      final String clusterName = indexDefinition != null && indexDefinition.getClassName() != null ? defaultClusterName
          : manualClusterName;

      index.create(iName, indexDefinition, getDatabase(), clusterName, iClusterIdsToIndex, iProgressListener);
      addIndexInternal(index);

      setDirty();
      save();

      return getIndexInstance(index);
    } finally {
      releaseExclusiveLock();
    }
  }

  public OIndexManager dropIndex(final String iIndexName) {
    if (getDatabase().getTransaction().isActive())
      throw new IllegalStateException("Cannot drop an index inside a transaction");

    acquireExclusiveLock();
    try {
      final OIndex<?> idx = indexes.remove(iIndexName.toLowerCase());
      if (idx != null) {
        removeClassPropertyIndex(idx);

        idx.delete();
        setDirty();
        save();
      }
      return this;
    } finally {
      releaseExclusiveLock();
    }
  }

  private void removeClassPropertyIndex(final OIndex<?> idx) {
    final OIndexDefinition indexDefinition = idx.getDefinition();
    if (indexDefinition == null || indexDefinition.getClassName() == null)
      return;

    final Map<OMultiKey, Set<OIndex<?>>> map = classPropertyIndex.get(indexDefinition.getClassName().toLowerCase());

    if (map == null) {
      return;
    }

    final int paramCount = indexDefinition.getParamCount();

    for (int i = 1; i <= paramCount; i++) {
      final List<String> fields = normalizeFieldNames(indexDefinition.getFields().subList(0, i));
      final OMultiKey multiKey = new OMultiKey(fields);
      final Set<OIndex<?>> indexSet = map.get(multiKey);
      if (indexSet == null)
        continue;
      indexSet.remove(idx);
      if (indexSet.isEmpty()) {
        map.remove(multiKey);
      }
    }

    if (map.isEmpty())
      classPropertyIndex.remove(indexDefinition.getClassName().toLowerCase());
  }

  @Override
  protected void fromStream() {
    acquireExclusiveLock();
    try {
      final Collection<ODocument> idxs = document.field(CONFIG_INDEXES);

      if (idxs != null) {
        OIndexInternal<?> index;
        boolean configUpdated = false;
        for (final ODocument d : idxs) {
          try {
            index = OIndexes.createIndex(getDatabase(), (String) d.field(OIndexInternal.CONFIG_TYPE));
            if (((OIndexInternal<?>) index).loadFromConfiguration(d)) {
              addIndexInternal(index);
            } else
              configUpdated = true;

          } catch (Exception e) {
            OLogManager.instance().error(this, "Error on loading index by configuration: %s", e, d);
          }
        }

        if (configUpdated)
          save();
      }
    } finally {
      releaseExclusiveLock();
    }
  }

  /**
   * Binds POJO to ODocument.
   */
  @Override
  public ODocument toStream() {
    acquireExclusiveLock();
    try {
      document.setInternalStatus(ORecordElement.STATUS.UNMARSHALLING);

      try {
        final ORecordTrackedSet idxs = new ORecordTrackedSet(document);

        for (final OIndexInternal<?> i : indexes.values()) {
          idxs.add(i.updateConfiguration());
        }
        document.field(CONFIG_INDEXES, idxs, OType.EMBEDDEDSET);

      } finally {
        document.setInternalStatus(ORecordElement.STATUS.LOADED);
      }
      document.setDirty();

      return document;
    } finally {
      releaseExclusiveLock();
    }
  }

  @Override
  protected OIndex<?> getIndexInstance(final OIndex<?> iIndex) {
    return iIndex;
  }

  @Override
  public synchronized void rebuildIndexes() {
    if (rebuildIndexesThread != null)
      // BUILDING ALREADY IN PROGRESS
      return;

    final ODatabaseRecord db = getDatabase();

    // USE A NEW DB INSTANCE
    final ODatabaseDocumentTx newDb = new ODatabaseDocumentTx(db.getURL());

    rebuildIndexesThread = new Runnable() {
      @Override
      public void run() {
        ODatabaseRecordThreadLocal.INSTANCE.set(newDb);

        try {
          newDb.getStorage().getDataSegmentIdByName(OMetadata.DATASEGMENT_INDEX_NAME);
          // DROP AND RE-CREATE 'INDEX' DATA-SEGMENT AND CLUSTER
          newDb.getStorage().dropDataSegment(OMetadata.DATASEGMENT_INDEX_NAME);
          newDb.getStorage().dropCluster(OMetadata.CLUSTER_INDEX_NAME, false);

          newDb.addDataSegment(OMetadata.DATASEGMENT_INDEX_NAME, null);
          newDb.getStorage().addCluster(OClusterLocal.TYPE, OMetadata.CLUSTER_INDEX_NAME, null, OMetadata.DATASEGMENT_INDEX_NAME,
              true);

        } catch (IllegalArgumentException ex) {
          // OLD DATABASE: CREATE SEPARATE DATASEGMENT AND LET THE INDEX CLUSTER TO POINT TO IT
          OLogManager.instance().info(this, "Creating 'index' data-segment to store all the index content...");

          newDb.addDataSegment(OMetadata.DATASEGMENT_INDEX_NAME, null);
          final OCluster indexCluster = newDb.getStorage().getClusterById(
              newDb.getStorage().getClusterIdByName(OMetadata.CLUSTER_INDEX_NAME));
          try {
            indexCluster.set(ATTRIBUTES.DATASEGMENT, OMetadata.DATASEGMENT_INDEX_NAME);
            OLogManager.instance().info(this,
                "Data-segment 'index' create correctly. Indexes will store content into this data-segment");
          } catch (IOException e) {
            OLogManager.instance().error(this, "Error changing data segment for cluster 'index'", e);
          }
        }

        final Collection<? extends OIndex<?>> indexList = getIndexes();

        final List<OIndex<?>> automaticIndexes = new ArrayList<OIndex<?>>();
        for (OIndex<?> idx : indexList)
          if (idx.isAutomatic())
            automaticIndexes.add(idx);

        OLogManager.instance().info(this,
            "Start rebuilding of %d automatic indexes in background. During this phase queries could be slower",
            automaticIndexes.size());

        int i = 1;
        int ok = 0;
        int errors = 0;

        for (final OIndex<?> idx : automaticIndexes)
          try {
            OLogManager.instance().info(idx, "- rebuilding index %d/%d: '%s'...", i, automaticIndexes.size(), idx.getName());
            idx.rebuild(new OProgressListener() {
              long startTime;
              long lastDump;
              long lastCounter = 0;

              @Override
              public void onBegin(final Object iTask, final long iTotal) {
                startTime = System.currentTimeMillis();
                lastDump = startTime;
              }

              @Override
              public boolean onProgress(final Object iTask, final long iCounter, final float iPercent) {
                final long now = System.currentTimeMillis();
                if (now - lastDump > 10000) {
                  // DUMP EVERY 5 SECONDS FOR LARGE INDEXES
                  OLogManager.instance().info(idx, "--> %3.2f%% progress, %,d indexed so far (%,d items/sec)", iPercent, iCounter,
                      ((iCounter - lastCounter) / 10));
                  lastDump = now;
                  lastCounter = iCounter;
                }
                return true;
              }

              @Override
              public void onCompletition(final Object iTask, final boolean iSucceed) {
                OLogManager.instance().info(idx, "--> ok, indexed %,d items in %,d ms", idx.getSize(),
                    (System.currentTimeMillis() - startTime));
              }
            });

            ok++;
          } catch (Throwable e) {
            OLogManager.instance().info(idx, "--> error caught (" + e + "). Continue with remaining indexes...");
            errors++;
          } finally {
            ++i;
          }

        OLogManager.instance().info(this, "%d indexes rebuilt successfully, %d errors", ok, errors);
        rebuildIndexesThread = null;
      }
    };

    // START IT IN BACKGROUND
    newDb.setProperty(ODatabase.OPTIONS.SECURITY.toString(), Boolean.FALSE);
    newDb.open("admin", "nopass");

    new Thread(rebuildIndexesThread).start();

    // RE-SET THE OLD DATABASE
    ODatabaseRecordThreadLocal.INSTANCE.set(db);
  }
}