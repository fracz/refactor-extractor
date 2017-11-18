commit 07bd0a30f0a74a08d428d89b2c05a4949903dd5f
Author: Ali Beyad <ali@elastic.co>
Date:   Mon Nov 28 20:23:16 2016 -0500

    Improves allocation decider decision explanation messages (#21771)

    This commit improves the decision explanation messages,
    particularly for NO decisions, in the various AllocationDecider
    implementations by including the setting(s) in the explanation
    message that led to the decision.

    This commit also returns a THROTTLE decision instead of a NO
    decision when the concurrent rebalances limit has been reached
    in ConcurrentRebalanceAllocationDecider, because it more accurately
    reflects a temporary throttling that will turn into a YES decision
    once the number of concurrent rebalances lessens, as opposed to a
    more permanent NO decision (e.g. due to filtering).