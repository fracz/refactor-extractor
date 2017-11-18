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

package org.neo4j.server.rrd;

import java.util.Timer;
import java.util.TimerTask;

public class ScheduledJob
{
    private Job job;
    private boolean running = true;
    public Timer timer;

    public ScheduledJob( Job job, int runEvery_seconds )
    {
        this.job = job;

        timer = new Timer( "rrd" );
        timer.scheduleAtFixedRate( runJob, 0, runEvery_seconds * 1000 );
    }

    private TimerTask runJob = new TimerTask()
    {
        public void run()
        {
            if ( !running )
            {
                this.cancel();
            } else
            {
                job.run();
            }
        }
    };

    public void runEveryXSeconds( int seconds )
    {
    }

    public void stop()
    {
        running = false;
        timer.cancel();
    }
}