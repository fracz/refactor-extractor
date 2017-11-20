commit 073ddda42da015789db87d50c803e23d7da08a68
Author: Liam Miller-Cushon <cushon@google.com>
Date:   Tue Apr 28 13:50:10 2015 -0700

    Make Error Prone less thread-hostile

    Some checks rely on mutable per-instance state, which makes them un-threadsafe.
    This change refactors scanner creation to instantiate new instances of checks
    for each compilation.

    RELNOTES: N/A
    -------------
    Created by MOE: http://code.google.com/p/moe-java
    MOE_MIGRATED_REVID=92283383