/**
 * Copyright (c) 2002-2011 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.server.rest;

import static org.junit.Assert.assertEquals;

import java.util.Collection;

import org.junit.Test;
import org.neo4j.kernel.impl.annotations.Documented;
import org.neo4j.server.database.DatabaseBlockedException;
import org.neo4j.server.rest.domain.JsonHelper;
import org.neo4j.server.rest.domain.JsonParseException;
import org.neo4j.server.rest.web.PropertyValueException;
import org.neo4j.test.GraphDescription.Graph;
import org.neo4j.test.GraphDescription.NODE;

public class AutoIndexNodeFunctionalTest extends AbstractRestFunctionalTestBase
{
    /**
     * Find node by query from an automatic index.
     *
     * See Find node by query for the actual query syntax.
     */
    @Documented
    @Test
    @Graph( nodes = {@NODE(name = "I", setNameProperty = true )}, autoIndexNodes = true)
    public void shouldRetrieveFromAutoIndexByQuery()
            throws PropertyValueException
    {
        data.get();
        String entity = gen.get().expectedStatus( 200 ).get(
                nodeAutoIndexUri() + "?query=name:I" ).entity();

        Collection<?> hits = (Collection<?>) JsonHelper.jsonToSingleValue( entity );
        assertEquals( 1, hits.size() );
    }

    private String nodeAutoIndexUri()
    {
        return getDataUri() + "index/auto/node/";
    }

    @Documented
    @Test
    @Graph( nodes = {@NODE(name = "I", setNameProperty = true )}, autoIndexNodes = true)
    public void find_autoindexed_node_by_exact_match()
            throws PropertyValueException
    {
        data.get();
        String entity = gen.get().expectedStatus( 200 ).get(
                nodeAutoIndexUri() + "name/I" ).entity();

        Collection<?> hits = (Collection<?>) JsonHelper.jsonToSingleValue( entity );
        assertEquals( 1, hits.size() );
    }

    @Test
    @Documented
    @Graph( nodes = {@NODE(name = "I", setNameProperty = true )}, autoIndexNodes = true)
    public void AutoIndex_is_not_removable()
            throws DatabaseBlockedException, JsonParseException
    {
        data.get();
        gen.get().expectedStatus( 405 ).delete(
                nodeAutoIndexUri()).entity();
    }

    @Test
    @Graph( nodes = {@NODE(name = "I", setNameProperty = true )}, autoIndexNodes = true)
    @Documented
    public void items_can_not_be_added_manually_to_an_AutoIndex() throws Exception
    {
        data.get();
        String indexName = graphdb().index().getNodeAutoIndexer().getAutoIndex().getName();
        gen.get().expectedStatus( 405 ).payload(
                JsonHelper.createJsonFrom( getNodeUri( data.get().get( "I" ) ) ) ).post(
                getNodeIndexUri( indexName, "name", "I" ) ).entity();
    }

    @Test
    @Documented
    @Graph( nodes = {@NODE(name = "I", setNameProperty = true )}, autoIndexNodes = true)
    public void autoindexed_items_cannot_be_removed_manually()
            throws DatabaseBlockedException, JsonParseException
    {
        long id = data.get().get( "I" ).getId();
        String indexName = graphdb().index().getNodeAutoIndexer().getAutoIndex().getName();
        gen.get().expectedStatus( 405 ).delete(
                getDataUri() + "index/node/"+indexName+ "/name/I/"+ id).entity();
        gen.get().expectedStatus( 405 ).delete(
                getDataUri() + "index/node/"+indexName+ "/name/"+ id).entity();
        gen.get().expectedStatus( 405 ).delete(
                getDataUri() + "index/node/" + indexName + "/" + id ).entity();
    }
}