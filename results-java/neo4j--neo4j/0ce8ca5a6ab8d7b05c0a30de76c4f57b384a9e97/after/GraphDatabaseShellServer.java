/*
 * Copyright (c) 2002-2009 "Neo Technology,"
 *     Network Engine for Objects in Lund AB [http://neotechnology.com]
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
package org.neo4j.shell.kernel;

import java.io.Serializable;
import java.rmi.RemoteException;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.EmbeddedReadOnlyGraphDatabase;
import org.neo4j.shell.Session;
import org.neo4j.shell.ShellException;
import org.neo4j.shell.ShellServer;
import org.neo4j.shell.SimpleAppServer;
import org.neo4j.shell.impl.AbstractClient;
import org.neo4j.shell.impl.BashVariableInterpreter;
import org.neo4j.shell.impl.BashVariableInterpreter.Replacer;
import org.neo4j.shell.kernel.apps.GraphDatabaseApp;

/**
 * A {@link ShellServer} which contains common methods to use with a
 * graph database service.
 */
public class GraphDatabaseShellServer extends SimpleAppServer
{
    private GraphDatabaseService graphDb;
    private BashVariableInterpreter bashInterpreter;

    /**
     * @param graphDb the {@link GraphDatabaseService} instance to use with the
     * shell server.
     * @throws RemoteException if an RMI error occurs.
     */
    public GraphDatabaseShellServer( GraphDatabaseService graphDb )
        throws RemoteException
    {
        super();
        // addGraphDbApps();
        this.graphDb = graphDb;
        this.bashInterpreter = new BashVariableInterpreter();
        this.bashInterpreter.addReplacer( "W", new WorkingDirReplacer() );
        this.setProperty( AbstractClient.PROMPT_KEY, getShellPrompt() );
        this.setProperty( AbstractClient.TITLE_KEYS_KEY,
            ".*name.*,.*title.*" );
        this.setProperty( AbstractClient.TITLE_MAX_LENGTH, "40" );
    }

    protected String getShellPrompt()
    {
        String name = "neo4j-sh";
        if ( this.graphDb instanceof EmbeddedReadOnlyGraphDatabase )
        {
            name += "[readonly]";
        }
        name += " \\W$ ";
        return name;
    }

    @Override
    public String welcome()
    {
        return "Welcome to the Neo4j Shell! Enter 'help' for a list of commands";
    }

    @Override
    public Serializable interpretVariable( String key, Serializable value,
        Session session ) throws ShellException
    {
        Transaction tx = getDb().beginTx();
        try
        {
            Serializable result = value;
            if ( key.equals( AbstractClient.PROMPT_KEY ) )
            {
                result = this.bashInterpreter.interpret( (String) value, this,
                    session );
            }
            tx.success();
            return result;
        }
        finally
        {
            tx.finish();
        }
    }

    /**
     * @return the {@link GraphDatabaseService} instance given in the
     * constructor.
     */
    public GraphDatabaseService getDb()
    {
        return this.graphDb;
    }

    /**
     * A {@link Replacer} for the variable "w"/"W" which returns the current
     * working directory (Bash), i.e. the current node.
     */
    public static class WorkingDirReplacer implements Replacer
    {
        public String getReplacement( ShellServer server, Session session )
            throws ShellException
        {
            return GraphDatabaseApp.getDisplayName(
                ( GraphDatabaseShellServer ) server, session,
                GraphDatabaseApp.getCurrent(
                    ( GraphDatabaseShellServer ) server, session ),
                    false ).toString();
        }
    }
}