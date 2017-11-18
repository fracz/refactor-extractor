package com.thinkaurelius.titan.graphdb.idmanagement;

/**
 * Interface for determining the type of element a particular id refers to.
 *
 * @author Matthias Broecheler (me@matthiasb.com)
 */
public interface IDInspector {

    public boolean isRelationTypeId(long id);

    public boolean isEdgeLabelId(long id);

    public boolean isPropertyKeyId(long id);

    public boolean isVertexId(long id);

    public long getPartitionId(long id);

}