package org.neo4j.kernel.ha;

import org.neo4j.graphdb.GraphDatabaseService;

public class FakeBroker extends AbstractBroker
{
    private final Master master;

    public FakeBroker( Master master )
    {
        this.master = master;
    }

    public Master getMaster()
    {
        return master;
    }

    public Object instantiateMasterServer( GraphDatabaseService graphDb )
    {
        throw new UnsupportedOperationException();
    }
}