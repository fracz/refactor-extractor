/*
 *
 *  * Copyright 2014 Orient Technologies.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.orientechnologies.lucene.manager;

import com.orientechnologies.common.log.OLogManager;
import com.orientechnologies.lucene.OLuceneIndexType;
import com.orientechnologies.lucene.collections.LuceneResultSet;
import com.orientechnologies.lucene.collections.OSpatialCompositeKey;
import com.orientechnologies.lucene.query.QueryContext;
import com.orientechnologies.lucene.query.SpatialQueryContext;
import com.orientechnologies.orient.core.command.OCommandContext;
import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.id.OContextualRecordId;
import com.orientechnologies.orient.core.index.OCompositeKey;
import com.orientechnologies.orient.core.index.OIndexCursor;
import com.orientechnologies.orient.core.index.OIndexEngineException;
import com.orientechnologies.orient.core.index.OIndexKeyCursor;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.spatial.shape.OShapeBuilder;
import com.orientechnologies.orient.spatial.strategy.SpatialQueryBuilder;
import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.distance.DistanceUtils;
import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.Shape;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.search.*;
import org.apache.lucene.spatial.SpatialStrategy;
import org.apache.lucene.spatial.prefix.RecursivePrefixTreeStrategy;
import org.apache.lucene.spatial.prefix.tree.QuadPrefixTree;
import org.apache.lucene.spatial.prefix.tree.SpatialPrefixTree;
import org.apache.lucene.spatial.query.SpatialArgs;
import org.apache.lucene.spatial.query.SpatialOperation;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Version;

import java.io.IOException;
import java.util.*;

public class OLuceneGeoSpatialIndexManager extends OLuceneIndexManagerAbstract implements OLuceneSpatialIndexContainer {

  /** Earth ellipsoid major axis defined by WGS 84 in meters */
  public static final double    EARTH_SEMI_MAJOR_AXIS = 6378137.0;                                    // meters (WGS 84)

  /** Earth ellipsoid minor axis defined by WGS 84 in meters */
  public static final double    EARTH_SEMI_MINOR_AXIS = 6356752.314245;                               // meters (WGS 84)

  /** Earth mean radius defined by WGS 84 in meters */
  public static final double    EARTH_MEAN_RADIUS     = 6371008.7714D;                                // meters (WGS 84)

  /** Earth axis ratio defined by WGS 84 (0.996647189335) */
  public static final double    EARTH_AXIS_RATIO      = EARTH_SEMI_MINOR_AXIS / EARTH_SEMI_MAJOR_AXIS;

  /** Earth ellipsoid equator length in meters */
  public static final double    EARTH_EQUATOR         = 2 * Math.PI * EARTH_SEMI_MAJOR_AXIS;

  /** Earth ellipsoid polar distance in meters */
  public static final double    EARTH_POLAR_DISTANCE  = Math.PI * EARTH_SEMI_MINOR_AXIS;
  protected final OShapeBuilder factory;
  private SpatialContext        ctx;
  private SpatialStrategy       strategy;

  private SpatialQueryBuilder   queryStrategy;

  public OLuceneGeoSpatialIndexManager(String name, OShapeBuilder factory) {
    super(name);
    this.ctx = factory.getSpatialContext();
    this.factory = factory;
    SpatialPrefixTree grid = new QuadPrefixTree(ctx, 11);
    this.strategy = new RecursivePrefixTreeStrategy(grid, "location");
    queryStrategy = new SpatialQueryBuilder(this, factory);
  }

  @Override
  public IndexWriter openIndexWriter(Directory directory, ODocument metadata) throws IOException {
    Analyzer analyzer = getAnalyzer(metadata);
    Version version = getLuceneVersion(metadata);
    IndexWriterConfig iwc = new IndexWriterConfig(version, analyzer);
    iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
    return new IndexWriter(directory, iwc);
  }

  @Override
  public IndexWriter createIndexWriter(Directory directory, ODocument metadata) throws IOException {
    Analyzer analyzer = getAnalyzer(metadata);
    Version version = getLuceneVersion(metadata);
    IndexWriterConfig iwc = new IndexWriterConfig(version, analyzer);
    iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
    return new IndexWriter(directory, iwc);
  }

  @Override
  public void init() {

  }

  @Override
  public boolean contains(Object key) {
    return false;
  }

  @Override
  public boolean remove(Object key) {
    return false;
  }

  @Override
  public Object get(Object key) {

    try {
      if (key instanceof Map) {
        return newGeoSearch((Map<String, Object>) key);

      } else {
        return legacySearch(key);
      }
    } catch (Exception e) {
      OLogManager.instance().error(this, "Error on getting entry against Lucene index", e);
    }

    return null;
  }

  private Object newGeoSearch(Map<String, Object> key) throws Exception {
    return new LuceneResultSet(this, queryStrategy.build(key));

  }

  private Object legacySearch(Object key) throws IOException {
    if (key instanceof OSpatialCompositeKey) {
      final OSpatialCompositeKey newKey = (OSpatialCompositeKey) key;

      final SpatialOperation strategy = newKey.getOperation() != null ? newKey.getOperation() : SpatialOperation.Intersects;

      if (SpatialOperation.Intersects.equals(strategy))
        return searchIntersect(newKey, newKey.getMaxDistance(), newKey.getContext());
      else if (SpatialOperation.IsWithin.equals(strategy))
        return searchWithin(newKey, newKey.getContext());

    } else if (key instanceof OCompositeKey) {
      return searchIntersect((OCompositeKey) key, 0, null);
    }
    throw new OIndexEngineException("Unknown key" + key, null);

  }

  public Object searchIntersect(OCompositeKey key, double distance, OCommandContext context) throws IOException {

    double lat = ((Double) OType.convert(((OCompositeKey) key).getKeys().get(0), Double.class)).doubleValue();
    double lng = ((Double) OType.convert(((OCompositeKey) key).getKeys().get(1), Double.class)).doubleValue();
    SpatialOperation operation = SpatialOperation.Intersects;

    Point p = ctx.makePoint(lng, lat);
    SpatialArgs args = new SpatialArgs(operation, ctx.makeCircle(lng, lat,
        DistanceUtils.dist2Degrees(distance, DistanceUtils.EARTH_MEAN_RADIUS_KM)));
    Filter filter = strategy.makeFilter(args);
    IndexSearcher searcher = searcher();
    ValueSource valueSource = strategy.makeDistanceValueSource(p);
    Sort distSort = new Sort(valueSource.getSortField(false)).rewrite(searcher);

    return new LuceneResultSet(this,
        new SpatialQueryContext(context, searcher, new MatchAllDocsQuery(), filter, distSort).setSpatialArgs(args));
  }

  @Override
  public void onRecordAddedToResultSet(QueryContext queryContext, OContextualRecordId recordId, Document doc, ScoreDoc score) {

    SpatialQueryContext spatialContext = (SpatialQueryContext) queryContext;
    if (spatialContext.spatialArgs != null) {
      Point docPoint = (Point) ctx.readShape(doc.get(strategy.getFieldName()));
      double docDistDEG = ctx.getDistCalc().distance(spatialContext.spatialArgs.getShape().getCenter(), docPoint);
      final double docDistInKM = DistanceUtils.degrees2Dist(docDistDEG, DistanceUtils.EARTH_EQUATORIAL_RADIUS_KM);
      recordId.setContext(new HashMap<String, Object>() {
        {
          put("distance", docDistInKM);
        }
      });
    }
  }

  @Override
  public Document buildDocument(Object key) {
    return null;
  }

  @Override
  public Query buildQuery(Object query) {
    throw new UnsupportedOperationException();
  }

  public Object searchWithin(OSpatialCompositeKey key, OCommandContext context) throws IOException {

    Set<OIdentifiable> result = new HashSet<OIdentifiable>();

    Shape shape = factory.makeShape(key, ctx);
    if (shape == null)
      return null;
    SpatialArgs args = new SpatialArgs(SpatialOperation.IsWithin, shape);
    IndexSearcher searcher = searcher();

    Filter filter = strategy.makeFilter(args);

    return new LuceneResultSet(this, new SpatialQueryContext(context, searcher, new MatchAllDocsQuery(), filter));
  }

  @Override
  public void put(Object key, Object value) {

    if (key instanceof OCompositeKey) {
      OCompositeKey compositeKey = (OCompositeKey) key;
      Collection<OIdentifiable> container = (Collection<OIdentifiable>) value;
      for (OIdentifiable oIdentifiable : container) {
        addDocument(newGeoDocument(oIdentifiable, factory.makeShape(compositeKey, ctx)));
      }
    } else if (key instanceof OIdentifiable) {

      ODocument location = ((OIdentifiable) key).getRecord();
      Collection<OIdentifiable> container = (Collection<OIdentifiable>) value;
      for (OIdentifiable oIdentifiable : container) {
        addDocument(newGeoDocument(oIdentifiable, factory.fromDoc(location)));
      }
    }

  }

  @Override
  public Object getFirstKey() {
    return null;
  }

  @Override
  public Object getLastKey() {
    return null;
  }

  @Override
  public OIndexCursor iterateEntriesBetween(Object rangeFrom, boolean fromInclusive, Object rangeTo, boolean toInclusive,
      boolean ascSortOrder, ValuesTransformer transformer) {
    return null;
  }

  @Override
  public OIndexCursor iterateEntriesMajor(Object fromKey, boolean isInclusive, boolean ascSortOrder, ValuesTransformer transformer) {
    return null;
  }

  @Override
  public OIndexCursor iterateEntriesMinor(Object toKey, boolean isInclusive, boolean ascSortOrder, ValuesTransformer transformer) {
    return null;
  }

  @Override
  public OIndexCursor cursor(ValuesTransformer valuesTransformer) {
    return null;
  }

  @Override
  public OIndexKeyCursor keyCursor() {
    return null;
  }

  @Override
  public boolean hasRangeQuerySupport() {
    return false;
  }

  private Document newGeoDocument(OIdentifiable oIdentifiable, Shape shape) {

    FieldType ft = new FieldType();
    ft.setIndexed(true);
    ft.setStored(true);

    Document doc = new Document();

    doc.add(OLuceneIndexType.createField(RID, oIdentifiable.getIdentity().toString(), Field.Store.YES,
        Field.Index.NOT_ANALYZED_NO_NORMS));
    for (IndexableField f : strategy.createIndexableFields(shape)) {
      doc.add(f);
    }

    doc.add(new StoredField(strategy.getFieldName(), ctx.toString(shape)));
    return doc;
  }

  public SpatialStrategy getStrategy() {
    return strategy;
  }

  @Override
  public SpatialStrategy strategy() {
    return strategy;
  }
}