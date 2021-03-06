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
package com.orientechnologies.orient.core.sql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.orientechnologies.common.collection.OCompositeKey;
import com.orientechnologies.common.concur.resource.OSharedResource;
import com.orientechnologies.common.exception.OException;
import com.orientechnologies.common.parser.OStringParser;
import com.orientechnologies.common.profiler.OProfiler;
import com.orientechnologies.common.util.OPair;
import com.orientechnologies.orient.core.command.OBasicCommandContext;
import com.orientechnologies.orient.core.command.OCommandRequest;
import com.orientechnologies.orient.core.db.record.ODatabaseRecord;
import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.exception.OCommandExecutionException;
import com.orientechnologies.orient.core.exception.OQueryParsingException;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.index.OCompositeIndexDefinition;
import com.orientechnologies.orient.core.index.OIndex;
import com.orientechnologies.orient.core.index.OIndexDefinition;
import com.orientechnologies.orient.core.index.OIndexInternal;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.security.ODatabaseSecurityResources;
import com.orientechnologies.orient.core.metadata.security.ORole;
import com.orientechnologies.orient.core.record.ORecord;
import com.orientechnologies.orient.core.record.ORecordInternal;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.record.impl.ODocumentHelper;
import com.orientechnologies.orient.core.serialization.serializer.OStringSerializerHelper;
import com.orientechnologies.orient.core.sql.filter.OSQLFilterCondition;
import com.orientechnologies.orient.core.sql.filter.OSQLFilterItem;
import com.orientechnologies.orient.core.sql.filter.OSQLFilterItemField;
import com.orientechnologies.orient.core.sql.functions.OSQLFunctionRuntime;
import com.orientechnologies.orient.core.sql.functions.misc.OSQLFunctionCount;
import com.orientechnologies.orient.core.sql.operator.OIndexReuseType;
import com.orientechnologies.orient.core.sql.operator.OQueryOperator;
import com.orientechnologies.orient.core.sql.operator.OQueryOperatorBetween;
import com.orientechnologies.orient.core.sql.operator.OQueryOperatorEquals;
import com.orientechnologies.orient.core.sql.operator.OQueryOperatorIn;
import com.orientechnologies.orient.core.sql.operator.OQueryOperatorMajor;
import com.orientechnologies.orient.core.sql.operator.OQueryOperatorMajorEquals;
import com.orientechnologies.orient.core.sql.operator.OQueryOperatorMinor;
import com.orientechnologies.orient.core.sql.operator.OQueryOperatorMinorEquals;
import com.orientechnologies.orient.core.type.tree.OMVRBTreeRIDSet;

/**
 * Executes the SQL SELECT statement. the parse() method compiles the query and builds the meta information needed by the execute().
 * If the query contains the ORDER BY clause, the results are temporary collected internally, then ordered and finally returned all
 * together to the listener.
 *
 * @author Luca Garulli
 */
@SuppressWarnings("unchecked")
public class OCommandExecutorSQLSelect extends OCommandExecutorSQLResultsetAbstract {
  private static final String         KEYWORD_AS            = " AS ";
  public static final String          KEYWORD_SELECT        = "SELECT";
  public static final String          KEYWORD_ASC           = "ASC";
  public static final String          KEYWORD_DESC          = "DESC";
  public static final String          KEYWORD_ORDER         = "ORDER";
  public static final String          KEYWORD_BY            = "BY";
  public static final String          KEYWORD_ORDER_BY      = "ORDER BY";

  private Map<String, Object>         projections           = null;
  private List<OPair<String, String>> orderedFields;
  private Object                      flattenTarget;
  private boolean                     anyFunctionAggregates = false;
  private int                         fetchLimit            = -1;
  private OIdentifiable               lastRecord;
  private Iterator<OIdentifiable>     subiterator;

  /**
   * Compile the filter conditions only the first time.
   */
  public OCommandExecutorSQLSelect parse(final OCommandRequest iRequest) {
    super.parse(iRequest);

    final int pos = parseProjections();
    if (pos == -1)
      return this;

    int endPosition = text.length();

    if (context == null)
      context = new OBasicCommandContext();

    compiledFilter = OSQLEngine.getInstance().parseFromWhereCondition(text.substring(pos, endPosition), context);

    optimize();

    currentPos = compiledFilter.currentPos < 0 ? endPosition : compiledFilter.currentPos + pos;

    if (currentPos > -1 && currentPos < text.length()) {
      currentPos = OStringParser.jump(text, currentPos, " \r\n");

      final StringBuilder word = new StringBuilder();
      String w;

      while (currentPos > -1) {
        currentPos = OSQLHelper.nextWord(text, textUpperCase, currentPos, word, true);

        if (currentPos > -1) {
          w = word.toString();
          if (w.equals(KEYWORD_ORDER))
            parseOrderBy(word);
          else if (w.equals(KEYWORD_LIMIT))
            parseLimit(word);
          else if (w.equals(KEYWORD_SKIP))
            parseSkip(word);
          else
            throw new OCommandSQLParsingException("Invalid keyword '" + w + "'");
        }
      }
    }
    if (limit == 0 || limit < -1) {
      throw new IllegalArgumentException("Limit must be > 0 or = -1 (no limit)");
    }
    return this;
  }

  public boolean hasNext() {
    if (lastRecord == null)
      // GET THE NEXT
      lastRecord = next();

    // BROWSE ALL THE RECORDS
    return lastRecord != null;
  }

  public OIdentifiable next() {
    if (lastRecord != null) {
      // RETURN LATEST AND RESET IT
      final OIdentifiable result = lastRecord;
      lastRecord = null;
      return result;
    }

    if (subiterator == null) {
      if (target == null) {
        // GET THE RESULT
        executeSearch(null);
        applyFlatten();
        applyProjections();

        subiterator = new ArrayList<OIdentifiable>(getResult()).iterator();
        tempResult = null;
      } else
        subiterator = (Iterator<OIdentifiable>) target.iterator();
    }

    // RESUME THE LAST POSITION
    while (subiterator.hasNext()) {
      if (!executeSearchRecord(subiterator.next()))
        break;

      if (lastRecord != null)
        return lastRecord;
    }

    return lastRecord;
  }

  public void remove() {
    throw new UnsupportedOperationException("remove()");
  }

  public Iterator<OIdentifiable> iterator() {
    return this;
  }

  public Object execute(final Map<Object, Object> iArgs) {
    fetchLimit = getQueryFetchLimit();

    executeSearch(iArgs);
    applyFlatten();
    applyProjections();
    applyOrderBy();
    applyLimit();

    return handleResult();
  }

  protected void executeSearch(final Map<Object, Object> iArgs) {
    assignTarget(iArgs);

    if (target == null)
      // SEARCH WITHOUT USING TARGET (USUALLY WHEN INDEXES ARE INVOLVED)
      return;

    // BROWSE ALL THE RECORDS
    for (OIdentifiable id : target)
      if (!executeSearchRecord(id))
        break;
  }

  @Override
  protected boolean assignTarget(Map<Object, Object> iArgs) {
    if (!super.assignTarget(iArgs)) {
      if (compiledFilter.getTargetIndex() != null)
        searchInIndex();
      else
        throw new OQueryParsingException("No source found in query: specify class, cluster(s), index or single record(s). Use "
            + getSyntax());
    }
    return true;
  }

  protected boolean executeSearchRecord(final OIdentifiable id) {
    final ORecordInternal<?> record = id.getRecord();

    if (record == null || record.getRecordType() != ODocument.RECORD_TYPE)
      // SKIP IT
      return true;

    if (filter(record))
      if (!addResult(record))
        // END OF EXECUTION
        return false;

    return true;
  }

  protected boolean addResult(final OIdentifiable iRecord) {
    lastRecord = null;

    if (skip > 0) {
      skip--;
      return true;
    }

    lastRecord = iRecord instanceof ORecord<?> ? ((ORecord<?>) iRecord).copy() : iRecord.getIdentity().copy();
    lastRecord = applyProjections(lastRecord);

    resultCount++;

    if (lastRecord != null)
      if (anyFunctionAggregates || orderedFields != null || flattenTarget != null) {
        // ORDER BY CLAUSE: COLLECT ALL THE RECORDS AND ORDER THEM AT THE END
        if (tempResult == null)
          tempResult = new ArrayList<OIdentifiable>();

        tempResult.add(lastRecord);
      } else if (subiterator == null) {
        // CALL THE LISTENER NOW BECAUSE IS NTO BROWSING (subiterator==null)
        if (request.getResultListener() != null)
          request.getResultListener().result(lastRecord);
      }

    if (fetchLimit > -1 && resultCount >= fetchLimit)
      // BREAK THE EXECUTION
      return false;

    return true;
  }

  private int getQueryFetchLimit() {
    if (orderedFields != null) {
      return -1;
    }

    final int sqlLimit;
    final int requestLimit;

    if (limit > -1)
      sqlLimit = limit;
    else
      sqlLimit = -1;

    if (request.getLimit() > -1)
      requestLimit = request.getLimit();
    else
      requestLimit = -1;

    if (sqlLimit == -1)
      return requestLimit;

    if (requestLimit == -1)
      return sqlLimit;

    return Math.min(sqlLimit, requestLimit);
  }

  public Map<String, Object> getProjections() {
    return projections;
  }

  public List<OPair<String, String>> getOrderedFields() {
    return orderedFields;
  }

  protected void parseOrderBy(final StringBuilder word) {
    int newPos = OSQLHelper.nextWord(text, textUpperCase, currentPos, word, true);

    if (!KEYWORD_BY.equals(word.toString()))
      throw new OQueryParsingException("Expected keyword " + KEYWORD_BY);

    currentPos = newPos;

    String fieldName;
    String fieldOrdering;

    orderedFields = new ArrayList<OPair<String, String>>();
    while (currentPos != -1) {
      currentPos = OSQLHelper.nextWord(text, textUpperCase, currentPos, word, false, " =><");
      if (currentPos == -1)
        throw new OCommandSQLParsingException("Field name expected", text, currentPos);

      fieldName = word.toString();

      currentPos = OSQLHelper.nextWord(text, textUpperCase, currentPos, word, true);
      if (currentPos == -1 || word.toString().equals(KEYWORD_LIMIT))
        // END/NEXT CLAUSE: SET AS ASC BY DEFAULT
        fieldOrdering = KEYWORD_ASC;
      else {

        if (word.toString().endsWith(",")) {
          currentPos--;
          word.deleteCharAt(word.length() - 1);
        }

        if (word.toString().equals(KEYWORD_ASC))
          fieldOrdering = KEYWORD_ASC;
        else if (word.toString().equals(KEYWORD_DESC))
          fieldOrdering = KEYWORD_DESC;
        else
          throw new OCommandSQLParsingException("Ordering mode '" + word
              + "' not supported. Valid is 'ASC', 'DESC' or nothing ('ASC' by default)", text, currentPos);

        currentPos = OSQLHelper.nextWord(text, textUpperCase, currentPos, word, true);
      }

      orderedFields.add(new OPair<String, String>(fieldName, fieldOrdering));

      if (!word.toString().equals(",")) {
        // GO BACK
        currentPos -= word.length();
        break;
      }
    }

    if (orderedFields.size() == 0)
      throw new OCommandSQLParsingException("Order by field set was missed. Example: ORDER BY name ASC, salary DESC", text,
          currentPos);
  }

  @Override
  protected void searchInClasses() {
    final OClass cls = compiledFilter.getTargetClasses().keySet().iterator().next();

    if (searchForIndexes(cls))
      OProfiler.getInstance().updateCounter("Query.indexUsage", 1);
    else
      super.searchInClasses();
  }

  @SuppressWarnings("rawtypes")
  private boolean searchForIndexes(final OClass iSchemaClass) {
    final ODatabaseRecord database = getDatabase();
    database.checkSecurity(ODatabaseSecurityResources.CLASS, ORole.PERMISSION_READ, iSchemaClass.getName().toLowerCase());

    // Create set that is sorted by amount of fields in OIndexSearchResult items
    // so the most specific restrictions will be processed first.
    final List<OIndexSearchResult> indexSearchResults = new ArrayList<OIndexSearchResult>();

    // fetch all possible variants of subqueries that can be used in indexes.
    analyzeQueryBranch(iSchemaClass, compiledFilter.getRootCondition(), indexSearchResults);

    // most specific will be processed first
    Collections.sort(indexSearchResults, new Comparator<OIndexSearchResult>() {
      public int compare(final OIndexSearchResult searchResultOne, final OIndexSearchResult searchResultTwo) {
        return searchResultTwo.getFieldCount() - searchResultOne.getFieldCount();
      }
    });

    // go through all variants to choose which one can be used for index search.
    for (final OIndexSearchResult searchResult : indexSearchResults) {
      final int searchResultFieldsCount = searchResult.fields().size();

      final List<OIndex<?>> involvedIndexes = getInvolvedIndexes(iSchemaClass, searchResult);
      Collections.sort(involvedIndexes, new Comparator<OIndex>() {
        public int compare(final OIndex indexOne, final OIndex indexTwo) {
          return indexOne.getDefinition().getParamCount() - indexTwo.getDefinition().getParamCount();
        }
      });

      // go through all possible index for given set of fields.
      for (final OIndex index : involvedIndexes) {
        final OIndexDefinition indexDefinition = index.getDefinition();
        final OQueryOperator operator = searchResult.lastOperator;

        // we need to test that last field in query subset and field in index that has the same position
        // are equals.
        if (!(operator instanceof OQueryOperatorEquals)) {
          final String lastFiled = searchResult.lastField.getItemName(searchResult.lastField.getItemCount() - 1);
          final String relatedIndexField = indexDefinition.getFields().get(searchResult.fieldValuePairs.size());
          if (!lastFiled.equals(relatedIndexField))
            continue;
        }

        final List<Object> keyParams = new ArrayList<Object>(searchResultFieldsCount);
        // We get only subset contained in processed sub query.
        for (final String fieldName : indexDefinition.getFields().subList(0, searchResultFieldsCount)) {
          final Object fieldValue = searchResult.fieldValuePairs.get(fieldName);
          if (fieldValue != null)
            keyParams.add(fieldValue);
          else
            keyParams.add(searchResult.lastValue);
        }

        final Collection<OIdentifiable> result = operator.executeIndexQuery(index, keyParams, fetchLimit);
        if (result == null)
          continue;

        fillSearchIndexResultSet(result);
        return true;
      }
    }
    return false;
  }

  private List<OIndex<?>> getInvolvedIndexes(OClass iSchemaClass, OIndexSearchResult searchResultFields) {
    final Set<OIndex<?>> involvedIndexes = iSchemaClass.getInvolvedIndexes(searchResultFields.fields());

    final List<OIndex<?>> result = new ArrayList<OIndex<?>>(involvedIndexes.size());
    for (OIndex<?> involvedIndex : involvedIndexes) {
      if (searchResultFields.lastField.isLong()) {
        result.addAll(OIndexProxy.createdProxy(involvedIndex, searchResultFields.lastField, getDatabase()));
      } else {
        result.add(involvedIndex);
      }
    }

    return result;
  }

  private OIndexSearchResult analyzeQueryBranch(final OClass iSchemaClass, final OSQLFilterCondition iCondition,
      final List<OIndexSearchResult> iIndexSearchResults) {
    if (iCondition == null)
      return null;

    final OQueryOperator operator = iCondition.getOperator();
    if (operator == null)
      if (iCondition.getRight() == null && iCondition.getLeft() instanceof OSQLFilterCondition) {
        return analyzeQueryBranch(iSchemaClass, (OSQLFilterCondition) iCondition.getLeft(), iIndexSearchResults);
      } else {
        return null;
      }

    final OIndexReuseType indexReuseType = operator.getIndexReuseType(iCondition.getLeft(), iCondition.getRight());
    if (indexReuseType.equals(OIndexReuseType.INDEX_INTERSECTION)) {
      final OIndexSearchResult leftResult = analyzeQueryBranch(iSchemaClass, (OSQLFilterCondition) iCondition.getLeft(),
          iIndexSearchResults);
      final OIndexSearchResult rightResult = analyzeQueryBranch(iSchemaClass, (OSQLFilterCondition) iCondition.getRight(),
          iIndexSearchResults);

      if (leftResult != null && rightResult != null) {
        if (leftResult.canBeMerged(rightResult)) {
          final OIndexSearchResult mergeResult = leftResult.merge(rightResult);
          if (iSchemaClass.areIndexed(mergeResult.fields()))
            iIndexSearchResults.add(mergeResult);
          return leftResult.merge(rightResult);
        }
      }

      return null;
    } else if (indexReuseType.equals(OIndexReuseType.INDEX_METHOD)) {
      OIndexSearchResult result = createIndexedProperty(iCondition, iCondition.getLeft());
      if (result == null)
        result = createIndexedProperty(iCondition, iCondition.getRight());

      if (result == null)
        return null;

      if (checkIndexExistence(iSchemaClass, result))
        iIndexSearchResults.add(result);

      return result;
    }

    return null;
  }

  /**
   * Add SQL filter field to the search candidate list.
   *
   * @param iCondition
   *          Condition item
   * @param iItem
   *          Value to search
   * @return true if the property was indexed and found, otherwise false
   */
  private OIndexSearchResult createIndexedProperty(final OSQLFilterCondition iCondition, final Object iItem) {
    if (iItem == null || !(iItem instanceof OSQLFilterItemField))
      return null;

    if (iCondition.getLeft() instanceof OSQLFilterItemField && iCondition.getRight() instanceof OSQLFilterItemField)
      return null;

    final OSQLFilterItemField item = (OSQLFilterItemField) iItem;

    if (item.hasChainOperators() && !item.isFieldChain())
      return null;

    final Object origValue = iCondition.getLeft() == iItem ? iCondition.getRight() : iCondition.getLeft();

    if (iCondition.getOperator() instanceof OQueryOperatorBetween || iCondition.getOperator() instanceof OQueryOperatorIn) {
      return new OIndexSearchResult(iCondition.getOperator(), item.getFieldChain(), origValue);
    }

    final Object value = OSQLHelper.getValue(origValue);

    if (value == null)
      return null;

    return new OIndexSearchResult(iCondition.getOperator(), item.getFieldChain(), value);
  }

  private void fillSearchIndexResultSet(final Object indexResult) {
    if (indexResult != null) {
      if (indexResult instanceof Collection<?>) {
        Collection<OIdentifiable> indexResultSet = (Collection<OIdentifiable>) indexResult;

        for (OIdentifiable identifiable : indexResultSet) {
          ORecord<?> record = identifiable.getRecord();
          if (record == null)
            throw new OException("Error during loading record with id : " + identifiable.getIdentity());

          if (filter((ORecordInternal<?>) record)) {
            final boolean continueResultParsing = addResult(record);
            if (!continueResultParsing)
              break;
          }
        }
      } else {
        final ORecord<?> record = ((OIdentifiable) indexResult).getRecord();
        if (filter((ORecordInternal<?>) record))
          addResult(record);
      }
    }
  }

  protected int parseProjections() {
    int currentPos = 0;
    final StringBuilder word = new StringBuilder();

    currentPos = OSQLHelper.nextWord(text, textUpperCase, currentPos, word, true);
    if (!word.toString().equals(KEYWORD_SELECT))
      return -1;

    int fromPosition = textUpperCase.indexOf(KEYWORD_FROM_2FIND, currentPos);
    if (fromPosition == -1)
      throw new OQueryParsingException("Missed " + KEYWORD_FROM, text, currentPos);

    Object projectionValue;
    final String projectionString = text.substring(currentPos, fromPosition).trim();
    if (projectionString.length() > 0 && !projectionString.equals("*")) {
      // EXTRACT PROJECTIONS
      projections = new LinkedHashMap<String, Object>();
      final List<String> items = OStringSerializerHelper.smartSplit(projectionString, ',');

      String fieldName;
      int beginPos;
      int endPos;
      for (String projection : items) {
        projection = projection.trim();

        if (projections == null)
          throw new OCommandSQLParsingException("Projection not allowed with FLATTEN() operator");

        fieldName = null;
        endPos = projection.toUpperCase(Locale.ENGLISH).indexOf(KEYWORD_AS);
        if (endPos > -1) {
          // EXTRACT ALIAS
          fieldName = projection.substring(endPos + KEYWORD_AS.length()).trim();
          projection = projection.substring(0, endPos).trim();

          if (projections.containsKey(fieldName))
            throw new OCommandSQLParsingException("Field '" + fieldName
                + "' is duplicated in current SELECT, choose a different name");
        } else {
          // EXTRACT THE FIELD NAME WITHOUT FUNCTIONS AND/OR LINKS
          beginPos = projection.charAt(0) == '@' ? 1 : 0;

          endPos = extractProjectionNameSubstringEndPosition(projection);

          fieldName = endPos > -1 ? projection.substring(beginPos, endPos) : projection.substring(beginPos);

          fieldName = OStringSerializerHelper.getStringContent(fieldName);

          // FIND A UNIQUE NAME BY ADDING A COUNTER
          for (int fieldIndex = 2; projections.containsKey(fieldName); ++fieldIndex) {
            fieldName += fieldIndex;
          }
        }

        if (projection.toUpperCase(Locale.ENGLISH).startsWith("FLATTEN(")) {
          List<String> pars = OStringSerializerHelper.getParameters(projection);
          if (pars.size() != 1)
            throw new OCommandSQLParsingException("FLATTEN operator expects the field name as parameter. Example FLATTEN( out )");
          flattenTarget = OSQLHelper.parseValue(this, pars.get(0).trim(), context);

          // BY PASS THIS AS PROJECTION BUT TREAT IT AS SPECIAL
          projections = null;

          if (!anyFunctionAggregates && flattenTarget instanceof OSQLFunctionRuntime
              && ((OSQLFunctionRuntime) flattenTarget).aggregateResults())
            anyFunctionAggregates = true;

          continue;
        }

        projectionValue = OSQLHelper.parseValue(this, projection, context);
        projections.put(fieldName, projectionValue);

        if (!anyFunctionAggregates && projectionValue instanceof OSQLFunctionRuntime
            && ((OSQLFunctionRuntime) projectionValue).aggregateResults())
          anyFunctionAggregates = true;
      }
    }

    currentPos = fromPosition + KEYWORD_FROM.length() + 1;

    return currentPos;
  }

  protected int extractProjectionNameSubstringEndPosition(String projection) {
    int endPos;
    final int pos1 = projection.indexOf('.');
    final int pos2 = projection.indexOf('(');
    final int pos3 = projection.indexOf('[');
    if (pos1 > -1 && pos2 == -1 && pos3 == -1)
      endPos = pos1;
    else if (pos2 > -1 && pos1 == -1 && pos3 == -1)
      endPos = pos2;
    else if (pos3 > -1 && pos1 == -1 && pos2 == -1)
      endPos = pos3;
    else if (pos1 > -1 && pos2 > -1 && pos3 == -1)
      endPos = Math.min(pos1, pos2);
    else if (pos2 > -1 && pos3 > -1 && pos1 == -1)
      endPos = Math.min(pos2, pos3);
    else if (pos1 > -1 && pos3 > -1 && pos2 == -1)
      endPos = Math.min(pos1, pos3);
    else if (pos1 > -1 && pos2 > -1 && pos3 > -1) {
      endPos = Math.min(pos1, pos2);
      endPos = Math.min(endPos, pos3);
    } else
      endPos = -1;
    return endPos;
  }

  private void applyOrderBy() {
    if (orderedFields == null)
      return;

    ODocumentHelper.sort(getResult(), orderedFields);
    orderedFields.clear();
  }

  /**
   * Extract the content of collections and/or links and put it as result
   */
  private void applyFlatten() {
    if (flattenTarget == null)
      return;

    final List<OIdentifiable> finalResult = new ArrayList<OIdentifiable>();
    Object fieldValue;
    if (tempResult != null)
      for (OIdentifiable id : tempResult) {
        if (flattenTarget instanceof OSQLFilterItem)
          fieldValue = ((OSQLFilterItem) flattenTarget).getValue(id.getRecord(), context);
        else if (flattenTarget instanceof OSQLFunctionRuntime)
          fieldValue = ((OSQLFunctionRuntime) flattenTarget).getResult();
        else
          fieldValue = flattenTarget.toString();

        if (fieldValue != null)
          if (fieldValue instanceof Collection<?>) {
            for (Object o : ((Collection<?>) fieldValue)) {
              if (o instanceof OIdentifiable)
                finalResult.add(((OIdentifiable) o).getRecord());
            }
          } else
            finalResult.add((OIdentifiable) fieldValue);
      }

    tempResult = finalResult;
  }

  private OIdentifiable applyProjections(final OIdentifiable iRecord) {
    if (projections != null) {
      // APPLY PROJECTIONS
      final ODocument doc = iRecord.getRecord();
      final ODocument result = new ODocument().setOrdered(true);

      // ASSIGN A TEMPORARY RID TO ALLOW PAGINATION IF ANY
      ((ORecordId) result.getIdentity()).clusterId = -2;
      ((ORecordId) result.getIdentity()).clusterPosition = resultCount;

      boolean canExcludeResult = false;

      Object value;
      for (Entry<String, Object> projection : projections.entrySet()) {
        if (projection.getValue().equals("*")) {
          doc.copy(result);
          value = null;
        } else if (projection.getValue().toString().startsWith("$")) {
          value = context != null ? context.getVariable(projection.getValue().toString().substring(1)) : null;
        } else if (projection.getValue() instanceof OSQLFilterItemField)
          value = ((OSQLFilterItemField) projection.getValue()).getValue(doc, null);
        else if (projection.getValue() instanceof OSQLFunctionRuntime) {
          final OSQLFunctionRuntime f = (OSQLFunctionRuntime) projection.getValue();
          canExcludeResult = f.filterResult();
          value = f.execute(doc, this);
        } else
          value = projection.getValue();

        if (value != null)
          result.field(projection.getKey(), value);
      }

      if (canExcludeResult && result.isEmpty())
        // RESULT EXCLUDED FOR EMPTY RECORD
        return null;

      if (anyFunctionAggregates)
        return null;

      return result;
    }

    // INVOKE THE LISTENER
    return iRecord;
  }

  private void searchInIndex() {
    final OIndex<Object> index = (OIndex<Object>) getDatabase().getMetadata().getIndexManager()
        .getIndex(compiledFilter.getTargetIndex());

    if (index == null)
      throw new OCommandExecutionException("Target index '" + compiledFilter.getTargetIndex() + "' not found");

    // nothing was added yet, so index definition for manual index was not calculated
    if (index.getDefinition() == null)
      return;

    if (compiledFilter.getRootCondition() != null) {
      if (!"KEY".equalsIgnoreCase(compiledFilter.getRootCondition().getLeft().toString()))
        throw new OCommandExecutionException("'Key' field is required for queries against indexes");

      final OQueryOperator indexOperator = compiledFilter.getRootCondition().getOperator();
      if (indexOperator instanceof OQueryOperatorBetween) {
        final Object[] values = (Object[]) compiledFilter.getRootCondition().getRight();
        final Collection<ODocument> entries = index.getEntriesBetween(getIndexKey(index.getDefinition(), values[0]),
            getIndexKey(index.getDefinition(), values[2]));

        for (final OIdentifiable r : entries) {
          final boolean continueResultParsing = addResult(r);
          if (!continueResultParsing)
            break;
        }

      } else if (indexOperator instanceof OQueryOperatorMajor) {
        final Object value = compiledFilter.getRootCondition().getRight();
        final Collection<ODocument> entries = index.getEntriesMajor(getIndexKey(index.getDefinition(), value), false);

        parseIndexSearchResult(entries);
      } else if (indexOperator instanceof OQueryOperatorMajorEquals) {
        final Object value = compiledFilter.getRootCondition().getRight();
        final Collection<ODocument> entries = index.getEntriesMajor(getIndexKey(index.getDefinition(), value), true);

        parseIndexSearchResult(entries);
      } else if (indexOperator instanceof OQueryOperatorMinor) {
        final Object value = compiledFilter.getRootCondition().getRight();
        final Collection<ODocument> entries = index.getEntriesMinor(getIndexKey(index.getDefinition(), value), false);

        parseIndexSearchResult(entries);
      } else if (indexOperator instanceof OQueryOperatorMinorEquals) {
        final Object value = compiledFilter.getRootCondition().getRight();
        final Collection<ODocument> entries = index.getEntriesMinor(getIndexKey(index.getDefinition(), value), true);

        parseIndexSearchResult(entries);
      } else if (indexOperator instanceof OQueryOperatorIn) {
        final List<Object> origValues = (List<Object>) compiledFilter.getRootCondition().getRight();
        final List<Object> values = new ArrayList<Object>(origValues.size());
        for (Object val : origValues) {
          if (index.getDefinition() instanceof OCompositeIndexDefinition) {
            throw new OCommandExecutionException("Operator IN not supported yet.");
          }

          val = getIndexKey(index.getDefinition(), val);
          values.add(val);
        }

        final Collection<ODocument> entries = index.getEntries(values);

        parseIndexSearchResult(entries);
      } else {
        final Object right = compiledFilter.getRootCondition().getRight();
        final Object keyValue = getIndexKey(index.getDefinition(), right);

        final Object res;
        if (index.getDefinition().getParamCount() == 1) {
          res = index.get(keyValue);
        } else {
          final Object secondKey = getIndexKey(index.getDefinition(), right);
          res = index.getValuesBetween(keyValue, secondKey);
        }

        if (res != null)
          if (res instanceof Collection<?>)
            // MULTI VALUES INDEX
            for (final OIdentifiable r : (Collection<OIdentifiable>) res)
              addResult(createIndexEntryAsDocument(keyValue, r.getIdentity()));
          else
            // SINGLE VALUE INDEX
            addResult(createIndexEntryAsDocument(keyValue, ((OIdentifiable) res).getIdentity()));
      }

    } else {
      // for "select count(*) from index:" we do not need to fetch all index data.
      if (anyFunctionAggregates && projections.entrySet().size() == 1) {
        final Object projection = projections.values().iterator().next();
        if (projection instanceof OSQLFunctionRuntime
            && ((OSQLFunctionRuntime) projection).getRoot().equals(OSQLFunctionCount.NAME)) {
          final OSQLFunctionRuntime f = (OSQLFunctionRuntime) projection;
          f.setResult(index.getSize());
          return;
        }
      }

      final OIndexInternal<?> indexInternal = index.getInternal();
      if (indexInternal instanceof OSharedResource)
        ((OSharedResource) indexInternal).acquireExclusiveLock();

      try {
        // ADD ALL THE ITEMS AS RESULT
        for (Iterator<Entry<Object, Object>> it = index.iterator(); it.hasNext();) {
          final Entry<Object, Object> current = it.next();

          if (current.getValue() instanceof Collection<?>)
            for (OIdentifiable identifiable : ((OMVRBTreeRIDSet) current.getValue()))
              addResult(createIndexEntryAsDocument(current.getKey(), identifiable.getIdentity()));
          else
            addResult(createIndexEntryAsDocument(current.getKey(), (OIdentifiable) current.getValue()));
        }
      } finally {
        if (indexInternal instanceof OSharedResource)
          ((OSharedResource) indexInternal).releaseExclusiveLock();
      }
    }

    if (anyFunctionAggregates) {
      for (final Entry<String, Object> projection : projections.entrySet()) {
        if (projection.getValue() instanceof OSQLFunctionRuntime) {
          final OSQLFunctionRuntime f = (OSQLFunctionRuntime) projection.getValue();
          f.setResult(index.getSize());
        }
      }
    }
  }

  private Object getIndexKey(final OIndexDefinition indexDefinition, Object value) {
    if (indexDefinition instanceof OCompositeIndexDefinition) {
      if (value instanceof List) {
        final List<?> values = (List<?>) value;
        List<Object> keyParams = new ArrayList<Object>(values.size());

        for (Object o : values) {
          keyParams.add(OSQLHelper.getValue(o));
        }
        return indexDefinition.createValue(keyParams);
      } else {
        value = OSQLHelper.getValue(value);
        if (value instanceof OCompositeKey) {
          return value;
        } else {
          return indexDefinition.createValue(value);
        }
      }
    } else {
      return OSQLHelper.getValue(value);
    }
  }

  protected void parseIndexSearchResult(final Collection<ODocument> entries) {
    for (final ODocument document : entries) {
      final boolean continueResultParsing = addResult(document);
      if (!continueResultParsing)
        break;
    }
  }

  private ODocument createIndexEntryAsDocument(final Object iKey, final OIdentifiable iValue) {
    final ODocument doc = new ODocument().setOrdered(true);
    doc.field("key", iKey);
    doc.field("rid", iValue);
    doc.unsetDirty();
    return doc;
  }

  private void applyProjections() {
    if (anyFunctionAggregates) {
      // EXECUTE AGGREGATIONS
      Object value;
      final ODocument result = new ODocument().setOrdered(true);
      for (Entry<String, Object> projection : projections.entrySet()) {
        if (projection.getValue() instanceof OSQLFilterItemField)
          value = ((OSQLFilterItemField) projection.getValue()).getValue(result, null);
        else if (projection.getValue() instanceof OSQLFunctionRuntime) {
          final OSQLFunctionRuntime f = (OSQLFunctionRuntime) projection.getValue();
          value = f.getResult();
        } else
          value = projection.getValue();

        result.field(projection.getKey(), value);
      }

      request.getResultListener().result(result);
    }
  }

  private boolean checkIndexExistence(OClass iSchemaClass, OIndexSearchResult result) {
    if (!iSchemaClass.areIndexed(result.fields())) {
      return false;
    }

    if (result.lastField.isLong()) {
      final int fieldCount = result.lastField.getItemCount();
      OClass oClass = iSchemaClass.getProperty(result.lastField.getItemName(0)).getLinkedClass();

      for (int i = 1; i < fieldCount; i++) {
        if (!oClass.areIndexed(result.lastField.getItemName(i))) {
          return false;
        }

        oClass = oClass.getProperty(result.lastField.getItemName(i)).getLinkedClass();
      }
    }
    return true;
  }

  @Override
  public String getSyntax() {
    return "SELECT [<Projections>] FROM <Target> [WHERE <Condition>*] [ORDER BY <Fields>* [ASC|DESC]*] [LIMIT <MaxRecords>]";
  }
}