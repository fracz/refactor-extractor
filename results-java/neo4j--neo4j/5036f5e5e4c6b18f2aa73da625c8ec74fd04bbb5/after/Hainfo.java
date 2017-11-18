package org.neo4j.kernel;

import java.util.Collection;
import java.util.Map;

import org.neo4j.kernel.ha.MasterServer;
import org.neo4j.kernel.impl.ha.SlaveContext;
import org.neo4j.shell.AppCommandParser;
import org.neo4j.shell.Output;
import org.neo4j.shell.Session;
import org.neo4j.shell.kernel.apps.GraphDatabaseApp;

public class Hainfo extends GraphDatabaseApp
{
    @Override
    protected String exec( AppCommandParser parser, Session session, Output out ) throws Exception
    {
        HighlyAvailableGraphDatabase db = (HighlyAvailableGraphDatabase) getServer().getDb();
        MasterServer master = db.getMasterServerIfMaster();
        out.println( "I'm currently " + (master != null ? "master" : "slave") );

        if ( master != null )
        {
            out.println( "Connected slaves:" );
            for ( Map.Entry<Integer, Collection<SlaveContext>> entry : master.getOngoingTransactions().entrySet() )
            {
                out.println( "\tMachine ID: " + entry.getKey() );
                if ( entry.getValue() != null && !entry.getValue().isEmpty() )
                {
                    out.println( "\t\tOngoing transactions: " + entry.getValue() );
                }
            }
        }
        return null;
    }
}