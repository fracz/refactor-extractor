/*
 * Licensed to ElasticSearch and Shay Banon under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. ElasticSearch licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
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

package org.elasticsearch.search.query;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.search.dfs.AggregatedDfs;
import org.elasticsearch.transport.TransportRequest;

import java.io.IOException;

import static org.elasticsearch.search.dfs.AggregatedDfs.readAggregatedDfs;

/**
 *
 */
public class QuerySearchRequest extends TransportRequest {

    private long id;

    private AggregatedDfs dfs;

    public QuerySearchRequest() {
    }

    public QuerySearchRequest(SearchRequest request, long id, AggregatedDfs dfs) {
        super(request);
        this.id = id;
        this.dfs = dfs;
    }

    public long id() {
        return id;
    }

    public AggregatedDfs dfs() {
        return dfs;
    }

    @Override
    public void readFrom(StreamInput in) throws IOException {
        super.readFrom(in);
        id = in.readLong();
        dfs = readAggregatedDfs(in);
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        out.writeLong(id);
        dfs.writeTo(out);
    }
}