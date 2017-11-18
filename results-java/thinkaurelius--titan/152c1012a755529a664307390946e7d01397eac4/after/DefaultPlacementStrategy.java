package com.thinkaurelius.titan.graphdb.database.idassigner.placement;

import com.google.common.base.Preconditions;
import com.thinkaurelius.titan.graphdb.vertices.InternalTitanVertex;

import java.util.Map;

/**
 * (c) Matthias Broecheler (me@matthiasb.com)
 */

public class DefaultPlacementStrategy implements IDPlacementStrategy {

    private final int partitionID;

    public DefaultPlacementStrategy(int partitionID) {
        Preconditions.checkArgument(partitionID>=0);
        this.partitionID=partitionID;
    }

    public DefaultPlacementStrategy() {
        this(0);
    }

    @Override
    public long getPartition(InternalTitanVertex vertex) {
        return partitionID;
    }

    @Override
    public void getPartitions(Map<InternalTitanVertex, PartitionAssignment> vertices) {
        for (Map.Entry<InternalTitanVertex,PartitionAssignment> entry: vertices.entrySet()) {
            entry.setValue(new SimplePartitionAssignment(partitionID));
        }
    }

    @Override
    public boolean supportsBulkPlacement() {
        return true;
    }

    @Override
    public void setLocalPartitionBounds(int lowerID, int upperID, int idLimit) {
        if (lowerID<upperID) {
            Preconditions.checkArgument(lowerID<=partitionID);
            Preconditions.checkArgument(upperID>partitionID);
        } else {
            Preconditions.checkArgument((lowerID<=partitionID && partitionID<idLimit) ||
            (upperID>partitionID && partitionID>=0));
        }
    }

    @Override
    public void exhaustedPartition(int partitionID) {
        throw new IllegalStateException("Cannot use a different partition under this strategy!");
    }
}