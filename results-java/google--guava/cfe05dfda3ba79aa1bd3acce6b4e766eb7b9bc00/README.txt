commit cfe05dfda3ba79aa1bd3acce6b4e766eb7b9bc00
Author: jasexton <jasexton@google.com>
Date:   Wed Aug 31 21:04:06 2016 -0700

    Changes to common.graph. Sorry for the churn, but after this things should be settled down for a while (and we'll be launching v20 soon!). Changes include:

    -Revert the interface names back. "BasicGraph" is now "Graph" (BasicGraph was probably a a poor choice), and "Graph" is now "ValueGraph". The "node only" Graph interface is at the top of the interface hierarchy, and ValueGraph extends it.
    -To facilitate the above, the various graph interfaces do *not* specify equality, and the default implementations use reference (identity) equality.
    -Add static methods Graphs.equivalent(...) for users that want value-based equality.
    -Rename Endpoints to EndpointPair. Rename nodeA()/nodeB() to nodeU()/nodeV() and expose isOrdered().

    Minor changes:

    -Undid the small refactor from [] since the original structure makes it easier for the Immutable classes to have the right hierarchy (i.e. ImmutableValueGraph extends ImmutableGraph, ImmutableNetwork.asGraph() is an ImmutableGraph).
    -Added an "isEquivalentTo" method to NetworkSubject (formerly GraphSubject) in labs. Eventually we should get around to having a single truth import for both Graphs and Networks.
    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=131906631