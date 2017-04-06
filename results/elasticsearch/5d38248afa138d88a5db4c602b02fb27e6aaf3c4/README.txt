commit 5d38248afa138d88a5db4c602b02fb27e6aaf3c4
Author: Ali Beyad <ali@elastic.co>
Date:   Thu Oct 6 22:53:05 2016 -0400

    Shard Decision class improvements for Explain API (#20742)

    This commit improves the shard decision container class in the following
    ways:

     1. Renames UnassignedShardDecision to ShardAllocationDecision, so that
        the class can be used for general shard decisions, not just unassigned
        shard decisions.
     2. Changes ShardAllocationDecision to have the final decision as a Type
        instead of a Decision, because all the information needed from the final
        decision is contained in `Type`.
     3. Uses cached instances of ShardAllocationDecision for NO and THROTTLE
        decisions when no explanation is needed (which is the common case when
        executing reroute's as opposed to using the explain API).