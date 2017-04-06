commit 0cec37f3c3f191a775ebcef833a00d57d1f0fe94
Author: Boaz Leskes <b.leskes@gmail.com>
Date:   Mon Feb 23 15:02:04 2015 +0100

    Recovery: unify RecoveryState management to IndexShard and clean up semantics

    We keep track of the current stage of recovery using an instance of RecoveryState which is stored on the relevant IndexShard. At the moment changes to this object are made in many places of the code, which are charged of doing it in the right order, keeping track of timers and many more. Also the changes to shard state are decoupled from the recovery stages which caused #9503.

    This PR refactors this and brings all of the changes into IndexShard. It also makes all recovery follow the exact same stages and shortcut some. This is in order to keep things simple and always the same (those shortcuts didn't add anything, we ended doing it all anyway).

    Also, all timer management is now folded into RecoveryState and unit tests are added.

    This closes #9503 by moving the shard to post recovery only once the recovery is done (before they were decoupled), meaning that master promotion of the target shard to started can not cancel the recovery.

    Closes #9902