commit 7631b1383b2702d0cf844c49e54a2fb3794f3d52
Author: kimchy <kimchy@gmail.com>
Date:   Sun Feb 14 13:29:39 2010 +0200

    change broadcast support to be able to run on all shards replicas (in parallel) and not just one shard per replica group. Change flush and refresh to use broadcast and not replicaiton. Remove shards transport support since broadcast now does exactly the same, and refactor index status to use broadcast (across all shards).