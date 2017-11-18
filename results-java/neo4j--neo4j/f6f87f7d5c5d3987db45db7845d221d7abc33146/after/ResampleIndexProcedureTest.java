/*
 * Copyright (c) 2002-2017 "Neo Technology,"
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
package org.neo4j.kernel.builtinprocs;

import org.junit.Test;

import org.neo4j.kernel.api.schema.NodePropertyDescriptor;
import org.neo4j.kernel.api.ReadOperations;
import org.neo4j.kernel.api.exceptions.ProcedureException;
import org.neo4j.kernel.api.exceptions.Status;
import org.neo4j.kernel.api.exceptions.index.IndexNotFoundKernelException;
import org.neo4j.kernel.api.exceptions.schema.IndexSchemaRuleNotFoundException;
import org.neo4j.kernel.api.exceptions.schema.SchemaRuleNotFoundException;
import org.neo4j.kernel.api.schema.IndexDescriptor;
import org.neo4j.kernel.api.schema.IndexDescriptorFactory;
import org.neo4j.kernel.impl.api.index.IndexingService;
import org.neo4j.kernel.impl.api.index.sampling.IndexSamplingMode;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ResampleIndexProcedureTest
{
    private final ReadOperations operations = mock( ReadOperations.class );
    private final IndexingService indexingService = mock( IndexingService.class );
    private final IndexProcedures procedure =
            new IndexProcedures( new StubKernelTransaction( operations ), indexingService );

    @Test
    public void shouldThrowAnExceptionIfTheLabelDoesntExist() throws ProcedureException
    {
        when( operations.labelGetForName( "NonExistentLabel" ) ).thenReturn( -1 );

        try
        {
            procedure.resampleIndex( ":NonExistentLabel(prop)" );
            fail( "Expected an exception" );
        }
        catch ( ProcedureException e )
        {
            assertThat( e.status(), is( Status.Schema.LabelAccessFailed ) );
        }
    }

    @Test
    public void shouldThrowAnExceptionIfThePropertyKeyDoesntExist() throws ProcedureException
    {
        when( operations.propertyKeyGetForName( "nonExistentProperty" ) ).thenReturn( -1 );

        try
        {
            procedure.resampleIndex( ":Label(nonExistentProperty)" );
            fail( "Expected an exception" );
        }
        catch ( ProcedureException e )
        {
            assertThat( e.status(), is( Status.Schema.PropertyKeyAccessFailed ) );
        }
    }

    @Test
    public void shouldLookUpTheIndexByLabelIdAndPropertyKeyId()
            throws ProcedureException, SchemaRuleNotFoundException, IndexNotFoundKernelException
    {
        IndexDescriptor index = IndexDescriptorFactory.from( new NodePropertyDescriptor( 0, 0 ) );
        when( operations.labelGetForName( anyString() ) ).thenReturn( 123 );
        when( operations.propertyKeyGetForName( anyString() ) ).thenReturn( 456 );
        when( operations.indexGetForLabelAndPropertyKey( anyObject() ) ).thenReturn( index );

        procedure.resampleIndex( ":Person(name)" );

        verify( operations ).indexGetForLabelAndPropertyKey( new NodePropertyDescriptor( 123, 456 ) );
    }

    @Test
    public void shouldThrowAnExceptionIfTheIndexDoesNotExist()
            throws SchemaRuleNotFoundException, IndexNotFoundKernelException

    {
        NodePropertyDescriptor descriptor = new NodePropertyDescriptor( 0, 0 );
        when( operations.labelGetForName( anyString() ) ).thenReturn( 0 );
        when( operations.propertyKeyGetForName( anyString() ) ).thenReturn( 0 );
        when( operations.indexGetForLabelAndPropertyKey( anyObject() ) )
                .thenThrow( new IndexSchemaRuleNotFoundException( descriptor ) );

        try
        {
            procedure.resampleIndex( ":Person(name)" );
            fail( "Expected an exception" );
        }
        catch ( ProcedureException e )
        {
            assertThat( e.status(), is( Status.Schema.IndexNotFound ) );
        }
    }

    @Test
    public void shouldTriggerResampling() throws SchemaRuleNotFoundException, ProcedureException
    {
        IndexDescriptor index = IndexDescriptorFactory.from( new NodePropertyDescriptor( 123, 456 ) );
        when( operations.indexGetForLabelAndPropertyKey( anyObject() ) ).thenReturn( index );

        procedure.resampleIndex( ":Person(name)" );

        verify( indexingService ).triggerIndexSampling( index, IndexSamplingMode.TRIGGER_REBUILD_ALL );
    }
}