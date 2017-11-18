package org.neo4j.kernel.ha;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.impl.ha.Broker;

public abstract class AbstractBroker implements Broker
{
    private GraphDatabaseService db;

    public void setDb( GraphDatabaseService db )
    {
        this.db = db;
    }

    public GraphDatabaseService getDb()
    {
        return this.db;
    }

    public void setLastCommittedTxId( long txId )
    {
        throw new RuntimeException( "implement this" );
    }

    public boolean thisIsMaster()
    {
        return false;
    }

    public int getMyMachineId()
    {
        throw new UnsupportedOperationException();
    }
}