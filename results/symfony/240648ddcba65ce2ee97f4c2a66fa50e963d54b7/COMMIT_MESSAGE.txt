commit 240648ddcba65ce2ee97f4c2a66fa50e963d54b7
Merge: 1104112 3f055f7
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Thu Sep 25 08:52:12 2014 +0200

    minor #12013 [FrameworkBundle] Added unit-tests for GlobalVariables::getUser() (iltar)

    This PR was merged into the 2.6-dev branch.

    Discussion
    ----------

    [FrameworkBundle] Added unit-tests for GlobalVariables::getUser()

    | Q             | A
    | ------------- | ---
    | Bug fix?      | yes
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | -
    | License       | MIT
    | Doc PR        | -

    Tests added should explain that `getUser()` should return `null` when a string is found as user. If this is not correct, a PR should be made. However, this would result in a huge BC break due to people using `{% if app.user %}` which would return `null` if an anonymous token was found. If this suddenly returns a string, this check will fail.

    While at it, I have also added `getUser()` tests to verify the unhappy flow is working. These tests uncovered that  if `$container->get('security.token_storage')` fails, it will throw an exception rather than return `null`. This issue is now fixed.

    List of changes
    --------------------
    - The old `testGetUser` has been refactored to be tested with multiple variations of return types to verify the return type to work as the code tells.
    - `get('security.token_storage')` is now only executed if `has('security.token_storage')` returns true

    @fabpot I think this PR should be merged before 2.6, because it fixes an uncaught exception bug in my previous PR which splits the security context

    Commits
    -------

    3f055f7 Fixed a bug and added unit-tests for GlobalVariables