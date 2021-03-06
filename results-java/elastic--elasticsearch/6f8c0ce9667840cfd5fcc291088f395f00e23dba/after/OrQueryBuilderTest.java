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

import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.BooleanClause.Occur;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentParser;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.Matchers.equalTo;

@SuppressWarnings("deprecation")
public class OrQueryBuilderTest extends BaseQueryTestCase<OrQueryBuilder> {

    @Override
    protected Query createExpectedQuery(OrQueryBuilder queryBuilder, QueryParseContext context) throws QueryParsingException, IOException {
        if (queryBuilder.filters().isEmpty()) {
            return null;
        }
        BooleanQuery query = new BooleanQuery();
        for (QueryBuilder subQuery : queryBuilder.filters()) {
            query.add(subQuery.toQuery(context), Occur.SHOULD);
        }
        return query;
    }

    /**
     * @return an OrQueryBuilder with random limit between 0 and 20
     */
    @Override
    protected OrQueryBuilder createTestQueryBuilder() {
        OrQueryBuilder query = new OrQueryBuilder();
        int subQueries = randomIntBetween(1, 5);
        for (int i = 0; i < subQueries; i++ ) {
            query.add(RandomQueryBuilder.create(random()));
        }
        if (randomBoolean()) {
            query.queryName(randomAsciiOfLengthBetween(1, 10));
        }
        return query;
    }

    @Override
    protected void assertLuceneQuery(OrQueryBuilder queryBuilder, Query query, QueryParseContext context) {
        if (queryBuilder.queryName() != null) {
            Query namedQuery = context.copyNamedFilters().get(queryBuilder.queryName());
            assertThat(namedQuery, equalTo(query));
        }
    }

    /**
     * test corner case where no inner queries exist
     */
    @Test
    public void testNoInnerQueries() throws QueryParsingException, IOException {
        OrQueryBuilder orQuery = new OrQueryBuilder();
        assertNull(orQuery.toQuery(createContext()));
    }

    @Test(expected=QueryParsingException.class)
    public void testMissingFiltersSection() throws IOException {
        QueryParseContext context = createContext();
        String queryString = "{ \"or\" : {}";
        XContentParser parser = XContentFactory.xContent(queryString).createParser(queryString);
        context.reset(parser);
        assertQueryHeader(parser, OrQueryBuilder.PROTOTYPE.getName());
        context.indexQueryParserService().queryParser(OrQueryBuilder.PROTOTYPE.getName()).fromXContent(context);
    }
}