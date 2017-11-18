package com.thinkaurelius.titan.hadoop.formats.util.input;

import com.thinkaurelius.titan.diskstorage.keycolumnvalue.SliceQuery;
import com.thinkaurelius.titan.graphdb.database.RelationReader;
import com.thinkaurelius.titan.graphdb.types.TypeInspector;
import com.thinkaurelius.titan.hadoop.FaunusVertexQueryFilter;

/**
 * @author Matthias Broecheler (me@matthiasb.com)
 */
public interface TitanHadoopSetup {

    public TypeInspector getTypeInspector();

    public SystemTypeInspector getSystemTypeInspector();

    public RelationReader getRelationReader(long vertexid);

    public VertexReader getVertexReader();

    public SliceQuery inputSlice(FaunusVertexQueryFilter inputFilter);

    public void close();

}