/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.apache.cassandra.cql;

import java.util.List;

import org.apache.cassandra.thrift.ConsistencyLevel;

/**
 * Encapsulates a completely parsed SELECT query, including the target
 * column family, expression, result count, and ordering clause.
 *
 */
public class SelectStatement
{
    private final SelectExpression expression;
    private final String columnFamily;
    private final ConsistencyLevel cLevel;
    private final WhereClause clause;
    private final int numRecords;

    public SelectStatement(SelectExpression expression, String columnFamily, ConsistencyLevel cLevel,
            WhereClause clause, int numRecords)
    {
        this.expression = expression;
        this.columnFamily = columnFamily;
        this.cLevel = cLevel;
        this.clause = clause;
        this.numRecords = numRecords;
    }

    public boolean isKeyRange()
    {
        return clause.isKeyRange();
    }

    public List<Term> getKeys()
    {
        return clause.getKeys();
    }

    public Term getKeyStart()
    {
        return clause.getStartKey();
    }

    public Term getKeyFinish()
    {
        return clause.getFinishKey();
    }

    public List<Relation> getColumnRelations()
    {
        return clause.getColumnRelations();
    }

    public boolean isColumnRange()
    {
        return expression.isColumnRange();
    }

    public List<Term> getColumnNames()
    {
        return expression.getColumns();
    }

    public Term getColumnStart()
    {
        return expression.getStart();
    }

    public Term getColumnFinish()
    {
        return expression.getFinish();
    }

    public String getColumnFamily()
    {
        return columnFamily;
    }

    public boolean isColumnsReversed()
    {
        return expression.isColumnsReversed();
    }

    public ConsistencyLevel getConsistencyLevel()
    {
        return cLevel;
    }

    public int getNumRecords()
    {
        return numRecords;
    }

    public int getColumnsLimit()
    {
        return expression.getColumnsLimit();
    }
}