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


import org.junit.Test;
import org.neo4j.kernel.impl.annotations.Documented;
import org.neo4j.server.database.DatabaseBlockedException;
import org.neo4j.server.rest.domain.JsonHelper;
import org.neo4j.server.rest.domain.JsonParseException;
import org.neo4j.server.rest.web.PropertyValueException;
import org.neo4j.test.GraphDescription.Graph;
import org.neo4j.test.GraphDescription.NODE;
import org.neo4j.test.GraphDescription.PROP;
import org.neo4j.test.GraphDescription.REL;

public class AutoIndexFunctionalTest extends AbstractRestFunctionalTestBase
{
    /**
     * Find node by query from an automatic index.
     *
     * See Find node by query for the actual query syntax.
     */
    @Documented
    @Test
    @Graph( nodes = { @NODE( name = "I", setNameProperty = true ) }, autoIndexNodes = true )
    public void shouldRetrieveFromAutoIndexByQuery()
            throws PropertyValueException
    {
        data.get();
        assertSize(1, gen.get().expectedStatus( 200 ).get(
                nodeAutoIndexUri() + "?query=name:I" ).entity());
    }

    private String nodeAutoIndexUri()
    {
        return getDataUri() + "index/auto/node/";
    }

    /**
     * Automatic index nodes can be found via exact lookups with normal Index
     * REST syntax.
     */
    @Documented
    @Test
    @Graph( nodes = { @NODE( name = "I", setNameProperty = true ) }, autoIndexNodes = true )
    public void find_node_by_exact_match_from_an_automatic_index()
            throws PropertyValueException
    {
        data.get();
        assertSize(1, gen.get().expectedStatus( 200 ).get(
                nodeAutoIndexUri() + "name/I" ).entity());
    }

    @Test
    @Documented
    @Graph( nodes = { @NODE( name = "I", setNameProperty = true ) }, autoIndexNodes = true )
    public void Relationship_AutoIndex_is_not_removable() throws DatabaseBlockedException,
            JsonParseException
    {
        data.get();
        gen.get().expectedStatus( 405 ).delete( relationshipAutoIndexUri() ).entity();
    }
    @Test
    @Documented
    @Graph( nodes = { @NODE( name = "I", setNameProperty = true ) }, autoIndexNodes = true )
    public void AutoIndex_is_not_removable() throws DatabaseBlockedException,
            JsonParseException
    {
        data.get();
        gen.get().expectedStatus( 405 ).delete( nodeAutoIndexUri() ).entity();
    }

    @Test
    @Graph( nodes = { @NODE( name = "I", setNameProperty = true ) }, autoIndexNodes = true )
    @Documented
    public void items_can_not_be_added_manually_to_an_AutoIndex()
            throws Exception
    {
        data.get();
        String indexName = graphdb().index().getNodeAutoIndexer().getAutoIndex().getName();
        gen.get().expectedStatus( 405 ).payload(
                JsonHelper.createJsonFrom( getNodeUri( data.get().get( "I" ) ) ) ).post(
                getNodeIndexUri( indexName, "name", "I" ) ).entity();
    }

    @Test
    @Graph( nodes = { @NODE( name = "I" ), @NODE( name = "you" ) }, relationships = { @REL( start = "I", end = "you", type = "know", properties = { @PROP( key = "since", value = "today" ) } ) }, autoIndexRelationships = true )
    @Documented
    public void items_can_not_be_added_manually_to_a_Relationship_AutoIndex()
            throws Exception
    {
        data.get();
        String indexName = graphdb().index().getRelationshipAutoIndexer().getAutoIndex().getName();
        gen.get().expectedStatus( 405 ).payload(
                JsonHelper.createJsonFrom( getRelationshipUri( data.get().get( "I" ).getRelationships().iterator().next() ) ) ).post(
                getRelationshipIndexUri( indexName, "name", "I" ) ).entity();
    }

    @Test
    @Documented
    @Graph( nodes = { @NODE( name = "I", setNameProperty = true ) }, autoIndexNodes = true )
    public void autoindexed_items_cannot_be_removed_manually()
            throws DatabaseBlockedException, JsonParseException
    {
        long id = data.get().get( "I" ).getId();
        String indexName = graphdb().index().getNodeAutoIndexer().getAutoIndex().getName();
        gen.get().expectedStatus( 405 ).delete(
                getDataUri() + "index/node/" + indexName + "/name/I/" + id ).entity();
        gen.get().expectedStatus( 405 ).delete(
                getDataUri() + "index/node/" + indexName + "/name/" + id ).entity();
        gen.get().expectedStatus( 405 ).delete(
                getDataUri() + "index/node/" + indexName + "/" + id ).entity();
    }

    @Test
    @Documented
    @Graph( nodes = { @NODE( name = "I" ), @NODE( name = "you" ) }, relationships = { @REL( start = "I", end = "you", type = "know", properties = { @PROP( key = "since", value = "today" ) } ) }, autoIndexRelationships = true )
    public void autoindexed_relationships_cannot_be_removed_manually()
            throws DatabaseBlockedException, JsonParseException
    {
        long id = data.get().get( "I" ).getRelationships().iterator().next().getId();
        String indexName = graphdb().index().getRelationshipAutoIndexer().getAutoIndex().getName();
        gen.get().expectedStatus( 405 ).delete(
                getDataUri() + "index/relationship/" + indexName + "/since/today/" + id ).entity();
        gen.get().expectedStatus( 405 ).delete(
                getDataUri() + "index/relationship/" + indexName + "/since/" + id ).entity();
        gen.get().expectedStatus( 405 ).delete(
                getDataUri() + "index/relationship/" + indexName + "/" + id ).entity();
    }

    @Documented
    @Test
    @Graph( nodes = { @NODE( name = "I" ), @NODE( name = "you" ) }, relationships = { @REL( start = "I", end = "you", type = "know", properties = { @PROP( key = "since", value = "today" ) } ) }, autoIndexRelationships = true )
    public void Find_relationship_by_query_from_an_automatic_index()
            throws PropertyValueException
    {
        data.get();
        assertSize(1, gen.get().expectedStatus( 200 ).get(
                relationshipAutoIndexUri() + "?query=since:today" ).entity());
    }

    @Documented
    @Test
    @Graph( nodes = { @NODE( name = "I" ), @NODE( name = "you" ) }, relationships = { @REL( start = "I", end = "you", type = "know", properties = { @PROP( key = "since", value = "today" ) } ) }, autoIndexRelationships = true )
    public void Find_relationship_by_exact_match_from_an_automatic_index()
            throws PropertyValueException
    {
        data.get();
        assertSize(1, gen.get()
                .expectedStatus(200)
                .get(relationshipAutoIndexUri() + "since/today/")
                .entity());
    }

    private String relationshipAutoIndexUri()
    {
        return getDataUri() + "index/auto/relationship/";
    }
}