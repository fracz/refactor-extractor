commit 9c38e310c37ac669471cdbc236672a6646061847
Author: Adrian Cole <acole@pivotal.io>
Date:   Mon Feb 15 18:32:18 2016 +0800

    Decouples InMemorySpanStore and tests from zipkin-server

    This refactors InMemorySpanStore to not use Java 8 features. Now, it is
    inside the core jar vs being stuck in the zipkin-server package.

    The first consumer of this will be MockZipkinServer (#64)