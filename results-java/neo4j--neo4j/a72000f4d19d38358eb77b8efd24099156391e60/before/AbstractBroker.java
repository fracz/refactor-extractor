package org.neo4j.kernel.ha;

import java.util.Map;

import org.neo4j.graphdb.GraphDatabaseService;

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
        // Do nothing
    }

    public boolean thisIsMaster()
    {
        return false;
    }

    public int getMasterMachineId()
    {
        return -1;
    }

    public int getMyMachineId()
    {
        throw new UnsupportedOperationException();
    }

    public void shutdown()
    {
        // Do nothing
    }

    public void invalidateMaster()
    {
    }

    public static BrokerFactory wrapSingleBroker( final Broker broker )
    {
        return new BrokerFactory()
        {
            public Broker create( String storeDir, Map<String, String> graphDbConfig )
            {
                return broker;
            }
        };
    }
}