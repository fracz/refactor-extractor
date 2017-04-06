commit e1f530eb7a8d4876c61542c4da0c882f50e98376
Merge: 47a0a4c 5409852
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Tue Jun 4 17:03:43 2013 +0200

    merged branch jakzal/2.3-more-security-tests (PR #8177)

    This PR was merged into the 2.3 branch.

    Discussion
    ----------

    [Security] Added more tests

    | Q             | A
    | ------------- | ---
    | Bug fix?      | no
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | -
    | License       | MIT
    | Doc PR        | -

    Continuation of #8136

    Commits
    -------

    5409852 [Security] Added few new test cases for the HttpUtils and improved readability of existing tests.
    d6ab77e [Security] Added tests for the SwitchUserListener.
    cccd005 [Security] Added tests for the ContextListener.
    307bc91 [Security] Added a test to the BasicAuthenticationListener.
    314f29a [Security] Removed an unnecessary call to sprintf() and added a test case.