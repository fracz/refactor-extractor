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
package org.neo4j.shell.kernel.apps;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.helpers.Service;
import org.neo4j.shell.App;
import org.neo4j.shell.AppCommandParser;
import org.neo4j.shell.OptionDefinition;
import org.neo4j.shell.OptionValueType;
import org.neo4j.shell.Output;
import org.neo4j.shell.Session;
import org.neo4j.shell.ShellException;
import org.neo4j.shell.TextUtil;
import org.neo4j.shell.impl.RelationshipToNodeIterable;

/**
 * Mimics the POSIX application with the same name, i.e. traverses to a node.
 */
@Service.Implementation( App.class )
public class Cd extends GraphDatabaseApp
{
    private static final String START_ALIAS = "start";
    private static final String END_ALIAS = "end";

    /**
     * The {@link Session} key to use to store the current node and working
     * directory (i.e. the path which the client got to it).
     */
    public static final String WORKING_DIR_KEY = "WORKING_DIR";

    /**
     * Constructs a new cd application.
     */
    public Cd()
    {
        this.addOptionDefinition( "a", new OptionDefinition( OptionValueType.NONE,
            "Absolute id, new primitive doesn't need to be connected to the current one" ) );
        this.addOptionDefinition( "r", new OptionDefinition( OptionValueType.NONE,
            "Makes the supplied id represent a relationship instead of a node" ) );
    }

    @Override
    public String getDescription()
    {
        return "Changes the current node or relationship, i.e. traverses " +
       		"one step to another node or relationship. Usage: cd <id>";
    }

    @Override
    public List<String> completionCandidates( String partOfLine, Session session )
    {
        String lastWord = TextUtil.lastWordOrQuoteOf( partOfLine, false );
        if ( lastWord.startsWith( "-" ) )
        {
            return super.completionCandidates( partOfLine, session );
        }
        try
        {
            TreeSet<String> result = new TreeSet<String>();
            NodeOrRelationship current = getCurrent( session );
            if ( current.isNode() )
            {
                // TODO Check if -r is supplied
                Node node = current.asNode();
                for ( Node otherNode : RelationshipToNodeIterable.wrap(
                        node.getRelationships(), node ) )
                {
                    long otherNodeId = otherNode.getId();
                    String title = findTitle( getServer(), session, otherNode );
                    if ( title != null )
                    {
                        if ( !result.contains( title ) )
                        {
                            maybeAddCompletionCandidate( result, title + "," + otherNodeId,
                                    lastWord );
                        }
                    }
                    maybeAddCompletionCandidate( result, "" + otherNodeId, lastWord );
                }
            }
            else
            {
                maybeAddCompletionCandidate( result, START_ALIAS, lastWord );
                maybeAddCompletionCandidate( result, END_ALIAS, lastWord );
                Relationship rel = current.asRelationship();
                maybeAddCompletionCandidate( result, "" + rel.getStartNode().getId(), lastWord );
                maybeAddCompletionCandidate( result, "" + rel.getEndNode().getId(), lastWord );
            }
            return new ArrayList<String>( result );
        }
        catch ( ShellException e )
        {
            e.printStackTrace();
            return super.completionCandidates( partOfLine, session );
        }
    }

    private static void maybeAddCompletionCandidate( Collection<String> candidates,
            String candidate, String lastWord )
    {
        if ( lastWord.length() == 0 || candidate.startsWith( lastWord ) )
        {
            candidates.add( candidate );
        }
    }

    @Override
    protected String exec( AppCommandParser parser, Session session,
        Output out ) throws ShellException, RemoteException
    {
        List<TypedId> paths = readPaths( session );

        NodeOrRelationship current = getCurrent( session );
        NodeOrRelationship newThing = null;
        if ( parser.arguments().isEmpty() )
        {
            newThing = NodeOrRelationship.wrap(
                getServer().getDb().getReferenceNode() );
            paths.clear();
        }
        else
        {
            String arg = parser.arguments().get( 0 );
            TypedId newId = current.getTypedId();
            if ( arg.equals( ".." ) )
            {
                if ( paths.size() > 0 )
                {
                    newId = paths.remove( paths.size() - 1 );
                }
            }
            else if ( arg.equals( "." ) )
            {
            }
            else if ( arg.equals( START_ALIAS ) || arg.equals( END_ALIAS ) )
            {
                newId = getStartOrEnd( current, arg );
                paths.add( current.getTypedId() );
            }
            else
            {
                long suppliedId = -1;
                try
                {
                    suppliedId = Long.parseLong( arg );
                }
                catch ( NumberFormatException e )
                {
                    suppliedId = findNodeWithTitle( current.asNode(), arg, session );
                    if ( suppliedId == -1 )
                    {
                        throw new ShellException( "No connected node with title '" + arg + "'" );
                    }
                }

                newId = parser.options().containsKey( "r" ) ?
                    new TypedId( NodeOrRelationship.TYPE_RELATIONSHIP, suppliedId ) :
                    new TypedId( NodeOrRelationship.TYPE_NODE, suppliedId );
                if ( newId.equals( current.getTypedId() ) )
                {
                    throw new ShellException( "Can't cd to where you stand" );
                }
                boolean absolute = parser.options().containsKey( "a" );
                if ( !absolute && !this.isConnected( current, newId ) )
                {
                    throw new ShellException(
                        getDisplayName( getServer(), session, newId, false ) +
                        " isn't connected to the current primitive," +
                        " use -a to force it to go there anyway" );
                }
                paths.add( current.getTypedId() );
            }
            newThing = this.getThingById( newId );
        }

        setCurrent( session, newThing );
        session.set( WORKING_DIR_KEY, this.makePath( paths ) );
        return null;
    }

    private long findNodeWithTitle( Node node, String match, Session session )
    {
        Object[] matchParts = splitNodeTitleAndId( match );
        if ( matchParts[1] != null )
        {
            return (Long) matchParts[1];
        }

        String titleMatch = (String) matchParts[0];
        for ( Node otherNode : RelationshipToNodeIterable.wrap( node.getRelationships(), node ) )
        {
            String title = findTitle( getServer(), session, otherNode );
            if ( titleMatch.equals( title ) )
            {
                return otherNode.getId();
            }
        }
        return -1;
    }

    private Object[] splitNodeTitleAndId( String string )
    {
        int index = string.lastIndexOf( "," );
        String title = null;
        Long id = null;
        try
        {
            id = Long.parseLong( string.substring( index + 1 ) );
            title = string.substring( 0, index );
        }
        catch ( NumberFormatException e )
        {
            title = string;
        }
        return new Object[] { title, id };
    }

    private TypedId getStartOrEnd( NodeOrRelationship current, String arg )
        throws ShellException
    {
        if ( !current.isRelationship() )
        {
            throw new ShellException( "Only allowed on relationships" );
        }
        Node newNode = null;
        if ( arg.equals( START_ALIAS ) )
        {
            newNode = current.asRelationship().getStartNode();
        }
        else if ( arg.equals( END_ALIAS ) )
        {
            newNode = current.asRelationship().getEndNode();
        }
        else
        {
            throw new ShellException( "Unknown alias '" + arg + "'" );
        }
        return NodeOrRelationship.wrap( newNode ).getTypedId();
    }

    private boolean isConnected( NodeOrRelationship current, TypedId newId )
        throws ShellException
    {
        if ( current.isNode() )
        {
            Node currentNode = current.asNode();
            for ( Relationship rel : currentNode.getRelationships() )
            {
                if ( newId.isNode() )
                {
                    if ( rel.getOtherNode( currentNode ).getId() ==
                        newId.getId() )
                    {
                        return true;
                    }
                }
                else
                {
                    if ( rel.getId() == newId.getId() )
                    {
                        return true;
                    }
                }
            }
        }
        else
        {
            if ( newId.isRelationship() )
            {
                return false;
            }

            Relationship relationship = current.asRelationship();
            if ( relationship.getStartNode().getId() == newId.getId() ||
                relationship.getEndNode().getId() == newId.getId() )
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Reads the session variable specified in {@link #WORKING_DIR_KEY} and
     * returns it as a list of typed ids.
     * @param session the session to read from.
     * @return the working directory as a list.
     * @throws RemoteException if an RMI error occurs.
     */
    public static List<TypedId> readPaths( Session session )
        throws RemoteException
    {
        List<TypedId> list = new ArrayList<TypedId>();
        String path = (String) session.get( WORKING_DIR_KEY );
        if ( path != null && path.trim().length() > 0 )
        {
            for ( String typedId : path.split( "," ) )
            {
                list.add( new TypedId( typedId ) );
            }
        }
        return list;
    }

    private String makePath( List<TypedId> paths )
    {
        StringBuffer buffer = new StringBuffer();
        for ( TypedId typedId : paths )
        {
            if ( buffer.length() > 0 )
            {
                buffer.append( "," );
            }
            buffer.append( typedId.toString() );
        }
        return buffer.length() > 0 ? buffer.toString() : null;
    }
}