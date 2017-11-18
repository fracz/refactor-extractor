commit 4125f012b94032ff91c25a71fb12fef4f470b3e5
Author: Simon Willnauer <simonw@apache.org>
Date:   Thu Mar 30 14:32:42 2017 +0200

    Streamline shard index availability in all SearchPhaseResults (#23788)

    Today we have the shard target and the target request ID available in SearchPhaseResults.
    Yet, the coordinating node maintains a shard index to reference the request, response tuples
    internally which is also used in many other classes to reference back from fetch results to
    query results etc. Today this shard index is implicitly passed via the index in AtomicArray
    which causes an undesirable dependency on this interface.
    This commit moves the shard index into the SearchPhaseResult and removes some dependencies
    on AtomicArray. Further removals will follow in the future. The most important refactoring here
    is the removal of AtomicArray.Entry which used to be created for every element in the atomic array
    to maintain the shard index during result processing. This caused an unnecessary indirection, dependency
    and potentially thousands of unnecessary objects in every search phase.