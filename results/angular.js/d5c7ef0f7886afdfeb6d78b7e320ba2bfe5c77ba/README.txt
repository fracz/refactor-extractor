commit d5c7ef0f7886afdfeb6d78b7e320ba2bfe5c77ba
Author: Igor Minar <igor@angularjs.org>
Date:   Fri Feb 7 12:14:32 2014 -0800

    revert: refactor(mocks): simplify the `inject` implementation

    This reverts commit 64d58a5b5292046adf8b28928950858ab3895fcc.

    For some weird reason this is causing regressions at Google.
    I'm not sure why and I'm running out of time to investigate, so I'm taking
    a safe route here and reverting the commit since it's just a refactoring.