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

import java.util.HashMap;
import java.util.Map;

import org.neo4j.rest.domain.Representation;

/**
 * Represents current lifecycle status and any action that the user has
 * requested.
 *
 * @author Jacob Hansson <jacob@voltvoodoo.com>
 *
 */
public class LifecycleRepresentation implements Representation
{

    public enum Status
    {
        RUNNING,
        STOPPED
    }

    public enum PerformedAction
    {
        STARTED,
        STOPPED,
        RESTARTED,
        NONE
    }

    protected Status currentStatus;
    protected PerformedAction performedAction;

    public LifecycleRepresentation( Status status )
    {
        this.currentStatus = status;
    }

    public LifecycleRepresentation( Status status, PerformedAction action )
    {
        this.performedAction = action;
        this.currentStatus = status;
    }

    public Object serialize()
    {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put( "current_status", currentStatus );
        if ( performedAction != null )
        {
            map.put( "action_performed", performedAction );
        }
        return map;
    }

}