/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.index.query;

import com.google.common.collect.Lists;

import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.BooleanClause.Occur;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.XContentBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A filter that matches documents matching boolean combinations of other filters.
 * @deprecated Use {@link BoolQueryBuilder} instead
 */
@Deprecated
public class OrQueryBuilder extends AbstractQueryBuilder<OrQueryBuilder> {

    public static final String NAME = "or";

    private final ArrayList<QueryBuilder> filters = Lists.newArrayList();

    static final OrQueryBuilder PROTOTYPE = new OrQueryBuilder();

    public OrQueryBuilder(QueryBuilder... filters) {
        for (QueryBuilder filter : filters) {
            this.filters.add(Objects.requireNonNull(filter));
        }
    }

    /**
     * Adds a filter to the list of filters to "or".
     * No <tt>null</tt> value allowed.
     */
    public OrQueryBuilder add(QueryBuilder filterBuilder) {
        filters.add(Objects.requireNonNull(filterBuilder));
        return this;
    }

    /**
     * @return the list of filters added to "or".
     */
    public List<QueryBuilder> filters() {
        return this.filters;
    }

    @Override
    protected void doXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject(NAME);
        builder.startArray("filters");
        for (QueryBuilder filter : filters) {
            filter.toXContent(builder, params);
        }
        builder.endArray();
        printBoostAndQueryName(builder);
        builder.endObject();
    }

    @Override
    protected Query doToQuery(QueryParseContext parseContext) throws IOException {
        if (filters.isEmpty()) {
            // no filters provided, this should be ignored upstream
            return null;
        }

        BooleanQuery query = new BooleanQuery();
        for (QueryBuilder f : filters) {
            Query innerQuery = f.toQuery(parseContext);
            // ignore queries that are null
            if (innerQuery != null) {
                query.add(innerQuery, Occur.SHOULD);
            }
        }
        if (query.clauses().isEmpty()) {
            // no inner lucene query exists, ignore upstream
            return null;
        }
        return query;
    }

    @Override
    public QueryValidationException validate() {
        return validateInnerQueries(filters, null);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    protected int doHashCode() {
        return Objects.hash(filters);
    }

    @Override
    protected boolean doEquals(OrQueryBuilder other) {
        return Objects.equals(filters, other.filters);
    }

    @Override
    protected OrQueryBuilder doReadFrom(StreamInput in) throws IOException {
        OrQueryBuilder orQueryBuilder = new OrQueryBuilder();
        List<QueryBuilder> queryBuilders = in.readNamedWriteableList();
        for (QueryBuilder queryBuilder : queryBuilders) {
            orQueryBuilder.add(queryBuilder);
        }
        return orQueryBuilder;

    }

    @Override
    protected void doWriteTo(StreamOutput out) throws IOException {
        out.writeNamedWriteableList(filters);
    }
}