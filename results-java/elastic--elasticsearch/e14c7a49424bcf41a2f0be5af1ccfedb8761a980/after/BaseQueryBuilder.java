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

import org.apache.lucene.search.Query;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.IOException;

/**
 * Base class with common code for all {@link QueryBuilder} implementations.
 */
public abstract class BaseQueryBuilder implements QueryBuilder {

    @Override
    public String toString() {
        try {
            XContentBuilder builder = XContentFactory.jsonBuilder();
            builder.prettyPrint();
            toXContent(builder, EMPTY_PARAMS);
            return builder.string();
        } catch (Exception e) {
            throw new ElasticsearchException("Failed to build query", e);
        }
    }

    @Override
    public BytesReference buildAsBytes() {
        return buildAsBytes(XContentType.JSON);
    }

    @Override
    public BytesReference buildAsBytes(XContentType contentType) {
        try {
            XContentBuilder builder = XContentFactory.contentBuilder(contentType);
            toXContent(builder, EMPTY_PARAMS);
            return builder.bytes();
        } catch (Exception e) {
            throw new ElasticsearchException("Failed to build query", e);
        }
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject();
        doXContent(builder, params);
        builder.endObject();
        return builder;
    }

    /**
     * Temporary default implementation for toQuery that parses the query using its query parser
     */
    //norelease to be removed once all query builders override toQuery providing their own specific implementation.
    public Query toQuery(QueryParseContext parseContext) throws QueryParsingException, IOException {
        return parseContext.indexQueryParserService().queryParser(parserName()).parse(parseContext);
    }

    /**
     * Temporary method that allows to retrieve the parser for each query.
     * @return the name of the parser class the default {@link #toQuery(QueryParseContext)} method delegates to
     */
    //norelease to be removed once all query builders override toQuery providing their own specific implementation.
    protected abstract String parserName();

    protected abstract void doXContent(XContentBuilder builder, Params params) throws IOException;

    @Override
    public QueryValidationException validate() {
        // default impl does not validate, subclasses should override.
        //norelease to be removed once all queries support validation
        return null;
    }
}