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

import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.elasticsearch.common.lucene.BytesRefs;
import org.elasticsearch.index.mapper.MappedFieldType;

/**
 * A Query that matches documents containing a term.
 */
public class TermQueryBuilder extends BaseTermQueryBuilder<TermQueryBuilder> implements BoostableQueryBuilder<TermQueryBuilder> {

    public static final String NAME = "term";
    static final TermQueryBuilder PROTOTYPE = new TermQueryBuilder(null, null);

    /** @see BaseTermQueryBuilder#BaseTermQueryBuilder(String, String) */
    public TermQueryBuilder(String fieldName, String value) {
        super(fieldName, (Object) value);
    }

    /** @see BaseTermQueryBuilder#BaseTermQueryBuilder(String, int) */
    public TermQueryBuilder(String fieldName, int value) {
        super(fieldName, (Object) value);
    }

    /** @see BaseTermQueryBuilder#BaseTermQueryBuilder(String, long) */
    public TermQueryBuilder(String fieldName, long value) {
        super(fieldName, (Object) value);
    }

    /** @see BaseTermQueryBuilder#BaseTermQueryBuilder(String, float) */
    public TermQueryBuilder(String fieldName, float value) {
        super(fieldName, (Object) value);
    }

    /** @see BaseTermQueryBuilder#BaseTermQueryBuilder(String, double) */
    public TermQueryBuilder(String fieldName, double value) {
        super(fieldName, (Object) value);
    }

    /** @see BaseTermQueryBuilder#BaseTermQueryBuilder(String, boolean) */
    public TermQueryBuilder(String fieldName, boolean value) {
        super(fieldName, (Object) value);
    }

    /** @see BaseTermQueryBuilder#BaseTermQueryBuilder(String, Object) */
    public TermQueryBuilder(String fieldName, Object value) {
        super(fieldName, value);
    }

    @Override
    public Query toQuery(QueryParseContext parseContext) {
        Query query = null;
        MappedFieldType mapper = parseContext.fieldMapper(this.fieldName);
        if (mapper != null) {
            query = mapper.termQuery(this.value, parseContext);
        }
        if (query == null) {
            query = new TermQuery(new Term(this.fieldName, BytesRefs.toBytesRef(this.value)));
        }
        query.setBoost(this.boost);
        if (this.queryName != null) {
            parseContext.addNamedQuery(queryName, query);
        }
        return query;
    }

    @Override
    protected TermQueryBuilder createBuilder(String fieldName, Object value) {
        return new TermQueryBuilder(fieldName, value);
    }

    @Override
    public String getName() {
        return NAME;
    }
}