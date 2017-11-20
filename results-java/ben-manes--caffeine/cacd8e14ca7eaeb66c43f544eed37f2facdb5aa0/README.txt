commit cacd8e14ca7eaeb66c43f544eed37f2facdb5aa0
Author: Ben Manes <ben.manes@gmail.com>
Date:   Mon Mar 23 15:19:03 2015 -0700

    Use relaxed reads for key

    The key fixed unless the node transitions to the retired or dead state under
    the node's lock. When it does, the entry should not be visible from the caller's
    perspective. This was audited to verify correct usages.

    The conditional remove was modified to follow the standard pattern of remove,
    then transition. It was implemented as a conditional transition followed by a
    remove. This was legacy from CLHM where compute() methods had not originally
    been available. By using computeIfPresent to conditionally remove, we have more
    assurance of the correct behavior. The previous may have even been wrong if the
    value had been weak/soft GC'd.

    Relaxed reads should offer a slight performance improvement due to avoiding
    unnecessary memory barriers.