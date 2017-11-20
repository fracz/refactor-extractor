commit f9ea34b52ace79c5f3e1d39fd0f1ade2eba9b06a
Author: glorioso <glorioso@google.com>
Date:   Thu Sep 1 10:26:48 2016 -0700

    Add check to CheckReturnValue to ignore circumstances where the CRV statement
    is the last statement in a block contained within an assertThrows or
    expectThrows.

    Also, refactored NextStatement to use enclosingNode(BlockTree.class);

    RELNOTES: CheckReturnValue will ignore a statement if it's the last statement
    in a block that's immediately passed to a JUnit Assert.assertThrows() or
    Assert.expectThrows()

    MOE_MIGRATED_REVID=131963202