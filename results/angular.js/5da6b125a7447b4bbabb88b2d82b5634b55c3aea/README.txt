commit 5da6b125a7447b4bbabb88b2d82b5634b55c3aea
Author: Igor Minar <igor@angularjs.org>
Date:   Thu Apr 18 12:50:49 2013 -0700

    test(modules): fix module tests which got disabled by ngMobile

    When ngMobile was merged in, we accidentaly included angular-scenario.js
    in the test file set for modules. Loading this file overrode jasmine's
    `it` and `describe` global functions which essentially disabled all of
    ~200 unit tests for wrapped modules.

    This change refactors the code to run the wrapped module tests.

    I had to extract browserTrigger from scenario runner in order to achieve
    this without code duplication.