package com.orientechnologies.lucene;

import java.util.Iterator;
import java.util.Map;

import com.orientechnologies.common.concur.resource.OSharedResourceAdaptiveExternal;
import com.orientechnologies.lucene.manager.OLuceneIndexManagerAbstract;
import com.orientechnologies.orient.core.config.OGlobalConfiguration;
import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.index.OIndex;
import com.orientechnologies.orient.core.index.OIndexCursor;
import com.orientechnologies.orient.core.index.OIndexDefinition;
import com.orientechnologies.orient.core.index.OIndexEngine;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.serialization.serializer.stream.OStreamSerializer;

/**
 * Created by enricorisa on 19/03/14.
 */
public class OLuceneIndexEngine<V> extends OSharedResourceAdaptiveExternal implements OIndexEngine<V> {

  private final String                  indexType;
  protected OLuceneIndexManagerAbstract lucene;
  protected OIndex                      indexManaged;
  private ODocument                     indexMetadata;

  public OLuceneIndexEngine(OLuceneIndexManagerAbstract delegate, String indexType) {
    super(OGlobalConfiguration.ENVIRONMENT_CONCURRENT.getValueAsBoolean(), OGlobalConfiguration.MVRBTREE_TIMEOUT
        .getValueAsInteger(), true);

    this.lucene = delegate;
    this.indexType = indexType;
  }

  @Override
  public void init() {
    lucene.init();
  }

  @Override
  public void flush() {
    lucene.commit();
  }

  @Override
  public void create(String indexName, OIndexDefinition indexDefinition, String clusterIndexName,
      OStreamSerializer valueSerializer, boolean isAutomatic) {

    lucene.createIndex(indexName, indexDefinition, clusterIndexName, valueSerializer, isAutomatic, indexMetadata);

  }

  @Override
  public void delete() {

    lucene.delete();

  }

  @Override
  public void deleteWithoutLoad(String indexName) {
    lucene.deleteWithoutLoad(indexName);
  }

  @Override
  public void load(ORID indexRid, String indexName, OIndexDefinition indexDefinition, boolean isAutomatic) {
    lucene.load(indexRid, indexName, indexDefinition, isAutomatic, indexMetadata);
  }

  @Override
  public boolean contains(Object key) {

    return lucene.contains(key);
  }

  @Override
  public boolean remove(Object key) {
    return lucene.remove(key);
  }

  public boolean remove(Object key, OIdentifiable value) {
    OIdentifiable rid = value;
    if (value instanceof ODocument) {
      rid = value.getIdentity();
    }
    return lucene.remove(key, rid);
  }

  @Override
  public ORID getIdentity() {
    return lucene.getIdentity();
  }

  @Override
  public void clear() {
    lucene.clear();
  }

  @Override
  public Iterator<Map.Entry<Object, V>> iterator() {
    return lucene.iterator();
  }

  @Override
  public Iterator<Map.Entry<Object, V>> inverseIterator() {
    return lucene.inverseIterator();
  }

  @Override
  public Iterator<V> valuesIterator() {
    return lucene.valuesIterator();
  }

  @Override
  public Iterator<V> inverseValuesIterator() {
    return lucene.inverseValuesIterator();
  }

  @Override
  public Iterable<Object> keys() {
    return null;
  }

  @Override
  public void unload() {
    lucene.commit();
  }

  @Override
  public void startTransaction() {
    lucene.startTransaction();
  }

  @Override
  public void stopTransaction() {
    lucene.commit();
  }

  @Override
  public void afterTxRollback() {
    lucene.rollback();
  }

  @Override
  public void afterTxCommit() {
    lucene.afterTxCommit();
  }

  @Override
  public void closeDb() {
    lucene.commit();
  }

  @Override
  public void close() {
    lucene.close();
  }

  @Override
  public void beforeTxBegin() {
    lucene.beforeTxBegin();
  }

  @Override
  public V get(Object key) {
    return (V) lucene.get(key);
  }

  @Override
  public void put(Object key, V value) {
    lucene.put(key, value);
  }

  @Override
  public Object getFirstKey() {
    return lucene.getFirstKey();
  }

  @Override
  public Object getLastKey() {
    return lucene.getLastKey();
  }

  @Override
  public void getValuesBetween(Object rangeFrom, boolean fromInclusive, Object rangeTo, boolean toInclusive, boolean ascSortOrder,
      ValuesTransformer<V> transformer, ValuesResultListener valuesResultListener) {

    lucene.getValuesBetween(rangeFrom, fromInclusive, rangeTo, toInclusive, ascSortOrder, transformer, valuesResultListener);
  }

  @Override
  public void getValuesMajor(Object fromKey, boolean isInclusive, boolean ascSortOrder, ValuesTransformer<V> transformer,
      ValuesResultListener valuesResultListener) {
    lucene.getValuesMajor(fromKey, isInclusive, ascSortOrder, transformer, valuesResultListener);
  }

  @Override
  public void getValuesMinor(Object toKey, boolean isInclusive, boolean ascSortOrder, ValuesTransformer<V> transformer,
      ValuesResultListener valuesResultListener) {
    lucene.getValuesMinor(toKey, isInclusive, ascSortOrder, transformer, valuesResultListener);
  }

  @Override
  public void getEntriesMajor(Object fromKey, boolean isInclusive, boolean ascSortOrder, ValuesTransformer<V> transformer,
      EntriesResultListener entriesResultListener) {

    lucene.getEntriesMajor(fromKey, isInclusive, ascSortOrder, transformer, entriesResultListener);
  }

  @Override
  public void getEntriesMinor(Object toKey, boolean isInclusive, boolean ascSortOrder, ValuesTransformer<V> transformer,
      EntriesResultListener entriesResultListener) {
    lucene.getEntriesMajor(toKey, isInclusive, ascSortOrder, transformer, entriesResultListener);
  }

  @Override
  public void getEntriesBetween(Object iRangeFrom, Object iRangeTo, boolean iInclusive, boolean ascSortOrder,
      ValuesTransformer<V> transformer, EntriesResultListener entriesResultListener) {
    lucene.getEntriesBetween(iRangeFrom, iRangeTo, iInclusive, ascSortOrder, transformer, entriesResultListener);
  }

  @Override
  public OIndexCursor iterateEntriesBetween(Object rangeFrom, boolean fromInclusive, Object rangeTo, boolean toInclusive,
      boolean ascSortOrder, ValuesTransformer<V> transformer) {
    return lucene.iterateEntriesBetween(rangeFrom, fromInclusive, rangeTo, toInclusive, ascSortOrder, transformer);
  }

  @Override
  public OIndexCursor iterateEntriesMajor(Object fromKey, boolean isInclusive, boolean ascSortOrder,
      ValuesTransformer<V> transformer) {
    return lucene.iterateEntriesMajor(fromKey, isInclusive, ascSortOrder, transformer);
  }

  @Override
  public OIndexCursor iterateEntriesMinor(Object toKey, boolean isInclusive, boolean ascSortOrder, ValuesTransformer<V> transformer) {
    return lucene.iterateEntriesMinor(toKey, isInclusive, ascSortOrder, transformer);
  }

  @Override
  public long size(ValuesTransformer<V> transformer) {
    return lucene.size(transformer);

  }

  @Override
  public boolean hasRangeQuerySupport() {
    return lucene.hasRangeQuerySupport();
  }

  public void setManagedIndex(OIndex index) {
    this.indexManaged = index;
  }

  public void setIndexMetadata(ODocument indexMetadata) {
    this.indexMetadata = indexMetadata;
  }

  public ODocument getIndexMetadata() {
    return indexMetadata;
  }

  public void setRebuilding(boolean rebuilding) {
    lucene.setRebuilding(rebuilding);
  }
}