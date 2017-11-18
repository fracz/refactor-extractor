commit 5e65c9828f424b6b0214b984b5d38c9cfc1c746f
Author: Googler <noreply@google.com>
Date:   Thu Aug 17 05:21:54 2017 +0200

    Change WalkableGraphFactory#prepareAndGet to take multiple SkyKeys as graph roots

    It also changes a few accessors of utility methods in Skyframe library. It
    refactors the QueryExpressionMapper to use a general QueryExpressionVisitor.

    RELNOTES: None
    PiperOrigin-RevId: 165534908