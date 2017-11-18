commit 035526d540998149ff1bfbb057c25b0dc9f419da
Author: lutovich <konstantin.lutovich@neotechnology.com>
Date:   Thu Dec 3 19:38:38 2015 +0100

    Fixed command aggregation for legacy indexes

    LegacyIndexTransactionStateImpl is responsible for tracking node/relationship
    additions and removals. It uses two separate maps for this. Each map has index
    name as a key and list of corresponding commands as a value. When index is
    dropped, corresponding list of commands is cleared.

    However additions to relationship legacy index ended up in the map dedicated
    to node legacy index and removals from relationship legacy index cleared
    commands from the node map. In particular this was a problem for node and
    relationship indexes that had same name. It could lead to missing entries in the
    legacy index after transaction completed successfully.

    This commit makes LegacyIndexTransactionStateImpl use relationship map for
    relationship commands.
    It also improves equals/hashCode methods for legacy index commands.