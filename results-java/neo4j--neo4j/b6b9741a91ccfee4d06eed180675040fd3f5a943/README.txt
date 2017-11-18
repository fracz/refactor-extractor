commit b6b9741a91ccfee4d06eed180675040fd3f5a943
Author: Chris Vest <mr.chrisvest@gmail.com>
Date:   Thu Jun 5 16:03:11 2014 +0200

    Backport: Speed up consistency checking of non-constraint indexes

    This improves the time complexity for consistency checking of normal non-constraint schema
    indexes from an O(n^2) to O(n).

    This is especielly noticable when many nodes are indexed by the same
    <label,propertyKey,propertyValue> tuples.

    The old code would go over all the nodes that were supposed to be indexed, and then for
    each of those nodes, do a lookup on the propertyValue, and then iterate through the result
    set of that look up, looking for the nodeId in question. This is obviously an oh-n-squared
    amount of work, if all the nodes are indexed with the same property value.

    The new code do an exact lookup for the combined <nodeId,propertyValue> in the given
    <label,propertyKey> index. For our purpose, we consider this a constant cost, and so our
    work now ends up being oh-n.

    Conflicts:
            community/kernel/src/test/java/org/neo4j/kernel/impl/api/index/inmemory/UniqueInMemoryIndexReader.java