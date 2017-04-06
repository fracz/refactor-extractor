commit e6a3c9c15308a6777f255334054c4ba1700d1210
Author: Simon Willnauer <simonw@apache.org>
Date:   Fri Apr 5 08:59:04 2013 +0200

    Improve integration testing by reusing an abstracted cluster across tests

    The new AbstractSharedClusterTest abstracts integration testing further to
    reduce the overhead of writing tests that don't rely on explict control over
    the cluster. For instance tests that run query, facets or that test highlighting
    don't need to explictly start and stop nodes. Testing features like the ones
    just mentioned are based on the assumption that the underlying cluster can
    be arbitray. Based on this assumption this base class allows to:

     * randomize cluster and index settings if not explictly specified
     * transparently test transport & node clients
     * test features like search or highlighting on different cluster sizes
     * allow reuse of node insteance across tests
     * provide utility methods that act as upper or lower bounds that a test must pass with
       ie. if a test requries at least 3 nodes then it should also pass with 4 nodes
     * given a cluster has unmodified cluster settings (persistent and transient) the cluster
       should not differ to a fresh started cluster when reused across nodes.
     * within a test the client implementation and the clients associated node can be changed
       at any time and should return a valid result.

    This patch also prepares some redundant tests like 'RelocationTests.java' for randomized
    testing. Test like this are very long-running on some machines and run the same test with
    different parameters like 'number of writers' or 'number of relocations' which can easily
    be chosen with a random number and run only ones during development but multiple times
    during CI builds.

    All the improvements in this change reduce the test time by ~30%