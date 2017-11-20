/*
 * Copyright 2014 Orient Technologies.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.orientechnologies.orient.spatial.shape;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.index.OCompositeKey;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.Rectangle;
import com.spatial4j.core.shape.Shape;
import com.spatial4j.core.shape.ShapeCollection;
import com.spatial4j.core.shape.jts.JtsGeometry;
import com.vividsolutions.jts.geom.Geometry;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

public class OShapeFactory extends OComplexShapeBuilder {

  private Map<String, OShapeBuilder> factories = new HashMap<String, OShapeBuilder>();

  public static final OShapeFactory  INSTANCE  = new OShapeFactory();

  protected OShapeFactory() {
    registerFactory(new OLineStringShapeBuilder());
    registerFactory(new OMultiLineStringShapeBuilder());
    registerFactory(new OPointShapeBuilder());
    registerFactory(new OMultiPointShapeBuilder());
    registerFactory(new ORectangleShapeBuilder());
    registerFactory(new OPolygonShapeBuilder());
    registerFactory(new OMultiPolygonShapeBuilder());
    registerFactory(new OGeometryCollectionShapeBuilder(this));
  }

  @Override
  public String getName() {
    return null;
  }

  @Override
  public OShapeType getType() {
    return null;
  }

  @Override
  public Shape makeShape(OCompositeKey key, SpatialContext ctx) {
    for (OShapeBuilder f : factories.values()) {
      if (f.canHandle(key)) {
        return f.makeShape(key, ctx);
      }
    }
    return null;
  }

  @Override
  public boolean canHandle(OCompositeKey key) {
    return false;
  }

  @Override
  public void initClazz(ODatabaseDocumentTx db) {
    for (OShapeBuilder f : factories.values()) {
      f.initClazz(db);
    }
  }

  @Override
  public Shape fromDoc(ODocument document) {
    OShapeBuilder oShapeBuilder = factories.get(document.getClassName());
    if (oShapeBuilder != null) {
      return oShapeBuilder.fromDoc(document);
    }
    // TODO handle exception shape not found
    return null;
  }

  @Override
  public Shape fromObject(Object obj) {

    if (obj instanceof String) {
      try {
        return fromText((String) obj);
      } catch (ParseException e) {
        // TODO handle parse shape exception
        e.printStackTrace();
      }
    }
    if (obj instanceof ODocument) {
      return fromDoc((ODocument) obj);
    }
    if (obj instanceof Map) {
      Map map = (Map) ((Map) obj).get("shape");
      if (map == null) {
        map = (Map) obj;
      }
      return fromMapGeoJson(map);
    }
    return null;
  }

  @Override
  public String asText(ODocument document) {
    OShapeBuilder oShapeBuilder = factories.get(document.getClassName());
    if (oShapeBuilder != null) {
      return oShapeBuilder.asText(document);
    }
    // TODO handle exception shape not found
    return null;
  }

  @Override
  public String asText(Object obj) {

    if (obj instanceof ODocument) {
      return asText((ODocument) obj);
    }
    if (obj instanceof Map) {
      Map map = (Map) ((Map) obj).get("shape");
      if (map == null) {
        map = (Map) obj;
      }
      return asText(map);
    }
    return null;
  }

  @Override
  public ODocument toDoc(Shape shape) {

    // TODO REFACTOR
    ODocument doc = null;
    if (Point.class.isAssignableFrom(shape.getClass())) {
      doc = factories.get(Point.class.getSimpleName()).toDoc(shape);
    } else if (Rectangle.class.isAssignableFrom(shape.getClass())) {
      doc = factories.get(Rectangle.class.getSimpleName()).toDoc(shape);
    } else if (JtsGeometry.class.isAssignableFrom(shape.getClass())) {
      JtsGeometry geometry = (JtsGeometry) shape;
      Geometry geom = geometry.getGeom();
      doc = factories.get(geom.getClass().getSimpleName()).toDoc(shape);

    } else if (ShapeCollection.class.isAssignableFrom(shape.getClass())) {
      ShapeCollection collection = (ShapeCollection) shape;

      if (isMultiPolygon(collection)) {
        doc = factories.get("MultiPolygon").toDoc(createMultiPolygon(collection));
      } else if (isMultiPoint(collection)) {
        doc = factories.get("MultiPoint").toDoc(createMultiPoint(collection));
      } else if (isMultiLine(collection)) {
        doc = factories.get("MultiLineString").toDoc(createMultiLine(collection));
      } else {
        doc = factories.get("GeometryCollection").toDoc(shape);
      }
    }
    return doc;
  }

  @Override
  public Shape fromMapGeoJson(Map geoJsonMap) {
    OShapeBuilder oShapeBuilder = factories.get(geoJsonMap.get("type"));
    if (oShapeBuilder != null) {
      return oShapeBuilder.fromMapGeoJson(geoJsonMap);
    }
    // TODO handle exception shape not found
    return null;
  }

  public void registerFactory(OShapeBuilder factory) {
    factories.put(factory.getName(), factory);
  }
}