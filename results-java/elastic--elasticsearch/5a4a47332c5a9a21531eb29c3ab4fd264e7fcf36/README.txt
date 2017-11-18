commit 5a4a47332c5a9a21531eb29c3ab4fd264e7fcf36
Author: Yannick Welsch <yannick@welsch.lu>
Date:   Wed Jun 28 15:48:47 2017 +0200

    Use a single method to update shard state

    This commit refactors index shard to provide a single method for
    updating the shard state on an incoming cluster state update.

    Relates #25431