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
package org.neo4j.shell.apps;

import java.io.Serializable;
import java.rmi.RemoteException;

import org.neo4j.helpers.Service;
import org.neo4j.shell.App;
import org.neo4j.shell.AppCommandParser;
import org.neo4j.shell.Output;
import org.neo4j.shell.Session;
import org.neo4j.shell.ShellException;
import org.neo4j.shell.impl.AbstractApp;

/**
 * Mimics the Bash application "env" and uses the client session {@link Session}
 * as the data container.
 */
@Service.Implementation( App.class )
public class Env extends AbstractApp
{
	@Override
	public String getDescription()
	{
		return "Lists all environment variables";
	}

	public String execute( AppCommandParser parser, Session session,
		Output out ) throws ShellException
	{
		try
		{
			for ( String key : session.keys() )
			{
				Serializable value = session.get( key );
				out.println( key + "=" + ( value == null ? "" : value ) );
			}
			return null;
		}
		catch ( RemoteException e )
		{
			throw new ShellException( e );
		}
	}
}