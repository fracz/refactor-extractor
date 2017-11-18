commit 892d8e699cf5ca3807da288bd08c73319c35c3b8
Author: Jonathan Ellis <jbellis@apache.org>
Date:   Fri Jan 10 12:08:04 2014 -0600

    refactor nodetool, encapsulating each command into a subclass
    patch by Clément Lardeur; reviewed by Mikhail Stepura for CASSANDRA-6381