commit 9a6d0e8621b8fcbfe8c6f178cd25d3c1d21e67af
Author: Cedric Champeau <cedric@gradle.com>
Date:   Mon May 8 10:57:19 2017 +0200

    Optimize service registry performance

    Our service registry infrastructure is far from being performant. Lookup by type is particularily expensive, and involves
    looping through all supplied providers, and eventually go to the parent registries until we find a match, that we can cache.
    This commit introduces a new method that allows a provider to tell if it is _candidate_ for providing a type (independently
    of parameterized types), and introduces more caching of the result.

    This improves performance of the service registry, at the cost of increased memory usage.