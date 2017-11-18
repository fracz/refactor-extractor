package org.neo4j.kernel.impl.management;

import javax.management.NotCompliantMBeanException;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.kernel.impl.core.NodeManager;
import org.neo4j.kernel.impl.nioneo.store.PropertyStore;
import org.neo4j.kernel.management.Primitives;

@Description( "Estimates of the numbers of different kinds of Neo4j primitives" )
class PrimitivesBean extends Neo4jMBean implements Primitives
{
    private final NodeManager nodeManager;

    PrimitivesBean( String instanceId, NodeManager nodeManager ) throws NotCompliantMBeanException
    {
        super( instanceId, Primitives.class );
        this.nodeManager = nodeManager;
    }

    @Description( "An estimation of the number of nodes used in this Neo4j instance" )
    public long getNumberOfNodeIdsInUse()
    {
        return nodeManager.getNumberOfIdsInUse( Node.class );
    }

    @Description( "An estimation of the number of relationships used in this Neo4j instance" )
    public long getNumberOfRelationshipIdsInUse()
    {
        return nodeManager.getNumberOfIdsInUse( Relationship.class );
    }

    @Description( "An estimation of the number of properties used in this Neo4j instance" )
    public long getNumberOfPropertyIdsInUse()
    {
        return nodeManager.getNumberOfIdsInUse( PropertyStore.class );
    }

    @Description( "The number of relationship types used in this Neo4j instance" )
    public long getNumberOfRelationshipTypeIdsInUse()
    {
        return nodeManager.getNumberOfIdsInUse( RelationshipType.class );
    }
}