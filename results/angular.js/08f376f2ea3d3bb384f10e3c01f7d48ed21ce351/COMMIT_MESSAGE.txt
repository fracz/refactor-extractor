commit 08f376f2ea3d3bb384f10e3c01f7d48ed21ce351
Author: Igor Minar <igor@angularjs.org>
Date:   Tue Oct 15 15:00:22 2013 -0700

    fix(csp): fix csp auto-detection and stylesheet injection

    When we refactored , we broke the csp mode because the previous implementation
    relied on the fact that it was ok to lazy initialize the .csp property, this
    is not the case any more.

    Besides, we need to know about csp mode during bootstrap and avoid injecting the
    stylesheet when csp is active, so I refactored the code to fix both issues.

    PR #4411 will follow up on this commit and add more improvements.

    Closes #917
    Closes #2963
    Closes #4394
    Closes #4444

    BREAKING CHANGE: triggering ngCsp directive via `ng:csp` attribute is not
    supported any more. Please use data-ng-csp instead.