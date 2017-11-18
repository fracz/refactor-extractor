commit 001bba137723a6b99599254a931938779c1fc9e8
Author: Chris Lang <clang@fb.com>
Date:   Wed Nov 28 14:39:39 2012 -0800

    [android-sdk] SharedPreferencesTokenCache was not caching AccessTokenSource.

    Summary:
    SharedPreferencesTokenCache did not handle serialization of Enums, which means that cached tokens would be loaded
    with their source set to AccessTokenSource.WEB_VIEW rather than the correct value. Added Enum support to this class,
    and did some minor refactoring of test code.

    Test Plan:
    - Added unit tests to validate this scenario
    - Ran all unit tests

    Revert Plan:

    Reviewers: mmarucheck, mingfli, karthiks

    Reviewed By: mingfli

    Differential Revision: https://phabricator.fb.com/D641964

    Task ID: 1919389