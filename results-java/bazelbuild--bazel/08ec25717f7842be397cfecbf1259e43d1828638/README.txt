commit 08ec25717f7842be397cfecbf1259e43d1828638
Author: Janak Ramakrishnan <janakr@google.com>
Date:   Thu Mar 12 01:05:15 2015 +0000

    Inform EvaluationProgressReceiver of nodes that are built in error.

    This allows the pending action counter to be decremented correctly when in --keep_going mode.

    As part of this, there's a small refactor in ParallelEvaluator that also fixes a potential bug, that nodes in error were signaling their parents with the graph version as opposed to their actual version. Because errors never compare equal (ErrorInfo doesn't override equality), this isn't an issue in practice. But it could be in the future.

    --
    MOS_MIGRATED_REVID=88395500