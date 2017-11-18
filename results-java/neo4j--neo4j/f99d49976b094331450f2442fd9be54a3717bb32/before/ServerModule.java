/**
 * Copyright (c) 2002-2011 "Neo Technology,"
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
package org.neo4j.server.modules;

import java.net.URI;
import java.util.Set;

import org.neo4j.server.NeoEmbeddedJettyServer;

/**
 *  An interface which the NeoServer uses to initialise server modules (e.g. JAX-RS, static content, webadmin)
 */
public interface ServerModule {

    /**
     * Start a module within the server
     *
     * @param neoServer The NeoServer that owns the module
     * @return a Set of URIs that are owned by the module, or an empty set if no URIs are claimed
     */
    public Set<URI> start(NeoEmbeddedJettyServer neoServer);

    public void stop();
}