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

/**
 * Relations encapsulate the relationship between an entity of some kind, and
 * a value (term). For example, KEY > 'start' or COLUMN = 1000L.
 *
 */
public class Relation
{
    public Entity entity = Entity.COLUMN;
    public RelationType type;
    public Term value;

    /**
     * Creates a new relation.
     *
     * @param entity the kind of relation this is; what the term is being compared to.
     * @param type the type that describes how this entity relates to the value.
     * @param value the value being compared.
     */
    public Relation(String entity, String type, Term value)
    {
        if (entity.toUpperCase().equals("KEY"))
            this.entity = Entity.KEY;

        this.type = RelationType.forString(type);
        this.value = value;
    }

    public boolean isKey()
    {
        return entity.equals(Entity.KEY);
    }

    public boolean isColumn()
    {
        return entity.equals(Entity.COLUMN);
    }
}

enum Entity
{
    KEY, COLUMN;
}

enum RelationType
{
    EQ, LT, LTE, GTE, GT;

    public static RelationType forString(String s)
    {
        if (s.equals("="))
            return EQ;
        else if (s.equals("<"))
            return LT;
        else if (s.equals("<="))
            return LTE;
        else if (s.equals(">="))
            return GTE;
        else if (s.equals(">"))
            return GT;

        return null;
    }
}