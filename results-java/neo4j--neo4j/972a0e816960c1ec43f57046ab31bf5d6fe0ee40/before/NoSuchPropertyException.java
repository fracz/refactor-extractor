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

package org.neo4j.webadmin.domain;

/**
 * Thrown when trying to set a non-existant property.
 *
 * @author Jacob Hansson <jacob@voltvoodoo.com>
 *
 */
public class NoSuchPropertyException extends RuntimeException
{
    /**
     * Serial #
     */
    private static final long serialVersionUID = -7766801456616236010L;

    public NoSuchPropertyException()
    {
        super();
    }

    public NoSuchPropertyException( String message, Throwable cause )
    {
        super( message, cause );
    }

    public NoSuchPropertyException( String message )
    {
        super( message );
    }

    public NoSuchPropertyException( Throwable cause )
    {
        super( cause );
    }
}