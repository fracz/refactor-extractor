/**
 * Copyright (c) 2002-2010 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.neo4j.index.impl.lucene;

import java.util.Collection;

import org.apache.lucene.document.Document;
import org.neo4j.graphdb.index.IndexHits;

class DocToIdIterator extends AbstractIndexHits<Long>
{
    private final Collection<Long> exclude;
    private IndexSearcherRef searcherOrNull;
    private final IndexHits<Document> source;

    DocToIdIterator( IndexHits<Document> source, Collection<Long> exclude, IndexSearcherRef searcherOrNull )
    {
        this.source = source;
        this.exclude = exclude;
        this.searcherOrNull = searcherOrNull;
    }

    @Override
    protected Long fetchNextOrNull()
    {
        Long result = null;
        while ( result == null )
        {
            if ( !source.hasNext() )
            {
                endReached();
                break;
            }
            Document doc = source.next();
            Long id = Long.parseLong(
                doc.getField( LuceneIndex.KEY_DOC_ID ).stringValue() );
            if ( exclude == null || !exclude.contains( id ) )
            {
                result = id;
            }
        }
        return result;
    }

    protected void endReached()
    {
        close();
    }

    @Override
    public void close()
    {
        if ( this.searcherOrNull != null )
        {
            this.searcherOrNull.closeStrict();
            this.searcherOrNull = null;
        }
    }

    public int size()
    {
        return source.size();
    }

    public float currentScore()
    {
        return source.currentScore();
    }
}