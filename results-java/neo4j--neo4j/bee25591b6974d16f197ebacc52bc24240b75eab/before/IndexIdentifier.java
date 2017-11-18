package org.neo4j.index.impl.lucene;

import java.util.Map;

class IndexIdentifier
{
    final String indexName;
    final Map<String, String> config;
    final EntityType entityType;
    final byte entityTypeByte;

    public IndexIdentifier( byte entityTypeByte, EntityType entityType, String indexName,
            Map<String, String> customConfig )
    {
        this.entityTypeByte = entityTypeByte;
        this.entityType = entityType;
        this.indexName = indexName;
        this.config = customConfig;
    }

    @Override
    public boolean equals( Object o )
    {
        if ( o == null || !getClass().equals( o.getClass() ) )
        {
            return false;
        }
        IndexIdentifier i = (IndexIdentifier) o;
        return entityTypeByte == i.entityTypeByte && indexName.equals( i.indexName );
    }

    @Override
    public int hashCode()
    {
        int code = 17;
        code += 7*entityTypeByte;
        code += 7*indexName.hashCode();
        return code;
    }
}