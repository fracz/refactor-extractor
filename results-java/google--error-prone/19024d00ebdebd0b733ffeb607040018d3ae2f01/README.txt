commit 19024d00ebdebd0b733ffeb607040018d3ae2f01
Author: glorioso <glorioso@google.com>
Date:   Wed Dec 21 11:05:52 2016 -0800

    Initial version of an open source refaster, with an example in the README.md.

    Incidental changes performed to get there
    * Shade in check_api to the core jar to allow end users to not need check_api
    if they have core
    * Per feedback, no longer fail the compile when refactorings are performed.

    RELNOTES: Barebones implementation of Refaster, a tool allowing for semantic refactorings of codebases using before-and-after templates.

    MOE_MIGRATED_REVID=142680811