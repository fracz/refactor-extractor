commit b25304644f07927716bafb9ca958515b07a9040e
Author: tinwelint <mattias@neotechnology.com>
Date:   Fri Nov 18 12:15:02 2016 +0100

    Introducting GBPTree, initial step for native indexing

    **WHAT?**
    This is the first step in building a generation-aware B+tree (GB+Tree) implementation directly atop a PageCache with no caching in between. Additionally internal and leaf nodes on same level are linked both left and right with sibling pointers, this to provide correct reading when concurrently modifying the tree.

    **WHY?**
    Initial prototyping has shown great performance improvement on label scan store writes compared to lucene.

    A GB+Tree could potentially be used for a lot of different types of indexes, path index for example.

    **DETAILS ON IMPLEMENTATION**
    *Non blocking*
    Tree is modified by a single writer, multiple concurrent readers are allowed. Both reading and writing is non blocking.

    *Garbage*
    Implementation aim to make reading and writing garbage free.

    *Build order*
    The 'index' module is built directly before 'kernel' module, see community package pom.

    **GENERATION**
    Every node and child/sibling pointer has a generation indicating when it first evolved (was created). During recovery, pointers and nodes can have been flushed to disk or lost from memory. To determine if a pointer or node can be 'trusted' to be stable on disk there is a global STABLE generation of the tree. This generation and every generation before it has been checkpointed (flushed to disk) and can therefore be trusted, even after a crash.

    The generation currently under evolution is considered to be UNSTABLE. Data belonging to or pointing to UNSTABLE generation can not be trusted after a crash and must be overwritten during recovery. Note that this is not implemented yet. After checkpoint the STABLE generation will be set to the UNSTABLE generation and UNSTABLE generation will be incremented.

    *GenSafePointer, GSP*
    By guarding the pointer fields, generation and address, with a checksum we can determine if a pointer is BROKEN (wrong checksum). A pointer can be broken if the page is flushed while writing the pointer and then crash before that page is flushed again.

    A pointer with generation and checksum is called a GenSafePointer, GSP.

    *GenSafePointerPair, GSPP*
    We always want to keep a stable version of every pointer. Therefore all pointers are in fact two GSPs, a GSPP. So when we read or write a child pointer for example, we look at the pair for pointers as a whole and determine which one to read from or write to, look at GenSafePointerPair class.

    **LabelScanStore**
    There is an implementation of a label scan store built on top of this that will be introduced in a separate PR.

    **Note**
    This is minimal feature set and among the things missing (but in pipeline) are:
    - Implement recovery
    - Implement freelist
    - Rebalance and merge when removing from tree