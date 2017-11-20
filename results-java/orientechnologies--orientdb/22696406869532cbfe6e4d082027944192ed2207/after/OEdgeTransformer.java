/*
 *
 *  * Copyright 2010-2014 Orient Technologies LTD (info(at)orientechnologies.com)
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

package com.orientechnologies.orient.etl.transformer;

import com.orientechnologies.orient.core.command.OBasicCommandContext;
import com.orientechnologies.orient.core.exception.OConfigurationException;
import com.orientechnologies.orient.core.index.OIndex;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.orientechnologies.orient.etl.OETLProcessHaltedException;
import com.orientechnologies.orient.etl.OETLProcessor;
import com.tinkerpop.blueprints.impls.orient.OrientBaseGraph;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;

import java.util.Collection;

public class OEdgeTransformer extends OAbstractTransformer {
  protected OrientBaseGraph              graph;
  protected String                       joinFieldName;
  protected String                       lookup;
  protected ACTION                       unresolvedLinkAction;
  protected OSQLSynchQuery<OrientVertex> sqlQuery;
  protected OIndex<?>                    index;
  protected String                       edgeClass = "E";

  protected enum ACTION {
    CREATE, WARNING, ERROR, HALT, SKIP
  }

  @Override
  public ODocument getConfiguration() {
    return new ODocument().fromJSON("{parameters:["
        + "{joinFieldName:{optional:false,description:'field name containing the value to join'}},"
        + "{class:{optional:true,description:'Edge class name. Default is \'E\''}},"
        + "{lookup:{optional:false,description:'<Class>.<property> or Query to execute'}},"
        + "{unresolvedVertexAction:{optional:true,description:'action when a unresolved vertices is found',values:"
        + stringArray2Json(ACTION.values()) + "}}]," + "input:['ODocument','OrientVertex'],output:'OrientVertex'}");
  }

  @Override
  public void configure(OETLProcessor iProcessor, final ODocument iConfiguration, final OBasicCommandContext iContext) {
    super.configure(iProcessor, iConfiguration, iContext);
    joinFieldName = iConfiguration.field("joinFieldName");

    if (iConfiguration.containsField("lookup"))
      lookup = iConfiguration.field("lookup");

    if (iConfiguration.containsField("unresolvedLinkAction"))
      unresolvedLinkAction = ACTION.valueOf(iConfiguration.field("unresolvedLinkAction").toString().toUpperCase());

    graph = processor.getGraphDatabase();
    edgeClass = iConfiguration.field("class");
    final OClass cls = graph.getEdgeType(edgeClass);
    if (cls == null)
      graph.createEdgeType(edgeClass);
  }

  @Override
  public String getName() {
    return "edge";
  }

  @Override
  public Object executeTransform(final Object input) {
    // GET JOIN VALUE
    Object joinValue;
    final OrientVertex vertex;
    if (input instanceof ODocument) {
      joinValue = ((ODocument) input).field(joinFieldName);
      vertex = graph.getVertex(input);
    } else {
      joinValue = ((OrientVertex) input).getProperty(joinFieldName);
      vertex = (OrientVertex) input;
    }

    if (joinValue != null) {
      Object result = null;

      if (sqlQuery == null && index == null) {
        // ONLY THE FIRST TIME
        if (lookup.toUpperCase().startsWith("SELECT"))
          sqlQuery = new OSQLSynchQuery<OrientVertex>(lookup);
        else
          index = processor.getDocumentDatabase().getMetadata().getIndexManager().getIndex(lookup);
      }

      if (sqlQuery != null)
        result = graph.command(sqlQuery).execute(joinValue);
      else {
        final OType idxFieldType = index.getDefinition().getTypes()[0];
        joinValue = idxFieldType.convert(joinValue, idxFieldType.getDefaultJavaType());
        result = index.get(joinValue);
      }

      if (result != null) {
        if (result instanceof Collection<?>) {
          if (!((Collection) result).isEmpty())
            result = ((Collection) result).iterator().next();
          else
            result = null;
        }

        if (result != null) {
          final OrientVertex targetVertex = graph.getVertex(result);

          // CREATE THE EDGE
          vertex.addEdge(edgeClass, targetVertex);
        }
      }

      if (result == null) {
        // APPLY THE STRATEGY DEFINED IN unresolvedLinkAction
        switch (unresolvedLinkAction) {
        case CREATE:
          if (lookup != null) {
            final String[] lookupParts = lookup.split("\\.");
            final OrientVertex linkedV = graph.addTemporaryVertex(lookupParts[0]);
            linkedV.setProperty(lookupParts[1], joinValue);
            linkedV.save();
            result = linkedV;
          } else
            throw new OConfigurationException("Cannot create linked document because target class is unknown. Use 'lookup' field");
          break;
        case ERROR:
          processor.getStats().incrementErrors();
          processor.out(true, "%s: ERROR Cannot resolve join for value '%s'", getName(), joinValue);
          break;
        case WARNING:
          processor.getStats().incrementWarnings();
          processor.out(true, "%s: WARN Cannot resolve join for value '%s'", getName(), joinValue);
          break;
        case SKIP:
          return null;
        case HALT:
          throw new OETLProcessHaltedException("Cannot resolve join for value '" + joinValue + "'");
        }
      }
    }

    return input;
  }
}