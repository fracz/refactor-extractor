commit a4ca63748f116c0e426be7710f0698518af2c043
Author: Ulf Adams <ulfjack@google.com>
Date:   Mon Jan 11 14:20:28 2016 +0000

    BuildView - untangle more of the methods that are only for ide info.

    In particular, don't immediately call into the ForTesting functions; I need to
    refactor some code that is called from here, and the semantics when called
    from ide info should not change. Changes to semantics when called from tests
    are much less problematic - we can simply run all the tests.

    --
    MOS_MIGRATED_REVID=111846384