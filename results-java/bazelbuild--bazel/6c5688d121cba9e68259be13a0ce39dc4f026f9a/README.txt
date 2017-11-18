commit 6c5688d121cba9e68259be13a0ce39dc4f026f9a
Author: Philipp Wollermann <philwo@google.com>
Date:   Tue Jun 9 12:35:06 2015 +0000

    Implement persistent worker processes and a spawn strategy that uses them.

    This will be used by the persistent JavaBuilder, which improves performance of Java compilation by 4x due to profiting from JVM JIT optimizations and not having to relaunch the JVM for every spawn.

    It is completely generic though, so as long as a tool (ProGuard? Dexer? Whatever.) conforms to the Worker process protocol, it can use the new spawn strategy.

    --
    MOS_MIGRATED_REVID=95527132