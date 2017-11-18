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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexProvider;
import org.neo4j.graphdb.index.RelationshipIndex;
import org.neo4j.helpers.collection.MapUtil;
import org.neo4j.kernel.Config;
import org.neo4j.kernel.impl.index.IndexConnectionBroker;
import org.neo4j.kernel.impl.index.ReadOnlyIndexConnectionBroker;
import org.neo4j.kernel.impl.transaction.TxModule;

public class LuceneIndexProvider extends IndexProvider
{
    static final String KEY_PROVIDER = "provider";
    static final String KEY_TYPE = "type";
    static final String KEY_ANALYZER = "analyzer";
    static final String KEY_TO_LOWER_CASE = "to_lower_case";
    static final String KEY_SIMILARITY = "similarity";
    public static final String SERVICE_NAME = "lucene";

    public static final Map<String, String> EXACT_CONFIG =
            Collections.unmodifiableMap( MapUtil.stringMap(
                    KEY_PROVIDER, SERVICE_NAME, KEY_TYPE, "exact" ) );

    public static final Map<String, String> FULLTEXT_CONFIG =
            Collections.unmodifiableMap( MapUtil.stringMap(
                    KEY_PROVIDER, SERVICE_NAME, KEY_TYPE, "fulltext",
                    KEY_TO_LOWER_CASE, "true" ) );

    public static final int DEFAULT_LAZY_THRESHOLD = 100;
    private static final String DATA_SOURCE_NAME = "lucene-index";

    private IndexConnectionBroker<LuceneXaConnection> broker;
    private LuceneDataSource dataSource;
    private GraphDatabaseService graphDb;
    final int lazynessThreshold = DEFAULT_LAZY_THRESHOLD;

    public LuceneIndexProvider()
    {
        super( SERVICE_NAME );
    }

    IndexConnectionBroker<LuceneXaConnection> broker()
    {
        return this.broker;
    }

    LuceneDataSource dataSource()
    {
        return this.dataSource;
    }

    GraphDatabaseService graphDb()
    {
        return this.graphDb;
    }

    @Override
    protected void load( KernelData kernel )
    {
    }

    @Override
    protected void init( KernelData kernel )
    {
        try
        {
            load( kernel.graphDatabase(), kernel.getConfig() );
        }
        catch ( RuntimeException e )
        {
            e.printStackTrace();
            throw e;
        }
    }

    private void load( GraphDatabaseService db, Config config )
    {
        this.graphDb = db;
        TxModule txModule = config.getTxModule();
        boolean isReadOnly = config.isReadOnly();
        Map<Object, Object> params = new HashMap<Object, Object>( config.getParams() );
        params.put( "read_only", isReadOnly );
        dataSource = (LuceneDataSource) txModule.registerDataSource( DATA_SOURCE_NAME,
                LuceneDataSource.class.getName(), LuceneDataSource.DEFAULT_BRANCH_ID,
                params, true );
        broker = isReadOnly ?
                new ReadOnlyIndexConnectionBroker<LuceneXaConnection>( txModule.getTxManager() ) :
                new ConnectionBroker( txModule.getTxManager(), dataSource );
    }

    public Index<Node> nodeIndex( String indexName, Map<String, String> config )
    {
        IndexIdentifier identifier = new IndexIdentifier( LuceneCommand.NODE,
                dataSource.nodeEntityType, indexName );
        synchronized ( dataSource.indexes )
        {
            LuceneIndex index = dataSource.indexes.get( identifier );
            if ( index == null )
            {
                index = new LuceneIndex.NodeIndex( this, identifier );
                dataSource.indexes.put( identifier, index );
            }
            return index;
        }
    }

    public RelationshipIndex relationshipIndex( String indexName, Map<String, String> config )
    {
        IndexIdentifier identifier = new IndexIdentifier( LuceneCommand.RELATIONSHIP,
                dataSource.relationshipEntityType, indexName );
        synchronized ( dataSource.indexes )
        {
            LuceneIndex index = dataSource.indexes.get( identifier );
            if ( index == null )
            {
                index = new LuceneIndex.RelationshipIndex( this, identifier );
                dataSource.indexes.put( identifier, index );
            }
            return (RelationshipIndex) index;
        }
    }

    public Map<String, String> fillInDefaults( Map<String, String> source )
    {
        Map<String, String> result = source != null ?
                new HashMap<String, String>( source ) : new HashMap<String, String>();
        String type = result.get( KEY_TYPE );
        if ( type == null )
        {
            type = "exact";
            result.put( KEY_TYPE, type );
        }
        if ( type.equals( "fulltext" ) )
        {
            if ( !result.containsKey( LuceneIndexProvider.KEY_TO_LOWER_CASE ) )
            {
                result.put( KEY_TO_LOWER_CASE, "true" );
            }
        }
        return result;
    }

    @Override
    public boolean configMatches( Map<String, String> storedConfig, Map<String, String> config )
    {
        return  match( storedConfig, config, KEY_TYPE, null ) &&
                match( storedConfig, config, KEY_TO_LOWER_CASE, "true" ) &&
                match( storedConfig, config, KEY_ANALYZER, null ) &&
                match( storedConfig, config, KEY_SIMILARITY, null );
    }

    private boolean match( Map<String, String> storedConfig, Map<String, String> config,
            String key, String defaultValue )
    {
        String value1 = storedConfig.get( key );
        String value2 = config.get( key );
        if ( value1 == null || value2 == null )
        {
            if ( value1 == value2 )
            {
                return true;
            }
            if ( defaultValue != null )
            {
                value1 = value1 != null ? value1 : defaultValue;
                value2 = value2 != null ? value2 : defaultValue;
                return value1.equals( value2 );
            }
        }
        else
        {
            return value1.equals( value2 );
        }
        return false;
    }

    @Override
    public String getDataSourceName()
    {
        return DATA_SOURCE_NAME;
    }
}