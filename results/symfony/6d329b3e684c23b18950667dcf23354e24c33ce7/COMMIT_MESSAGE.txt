commit 6d329b3e684c23b18950667dcf23354e24c33ce7
Merge: 04fb113 5b5a6b6
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Wed May 21 13:52:27 2014 +0200

    bug #10947 [2.4][PropertyAccess] Fixed getValue() when accessing non-existing indices of ArrayAccess implementations (webmozart)

    This PR was merged into the 2.4 branch.

    Discussion
    ----------

    [2.4][PropertyAccess] Fixed getValue() when accessing non-existing indices of ArrayAccess implementations

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

    This PR backports two test refactoring commits from the master branch. The third commit is basically the same as #10946.

    Commits
    -------

    5b5a6b6 [PropertyAccess] Fixed getValue() when accessing non-existing indices of ArrayAccess implementations
    91ee821 [PropertyAccess] Refactored PropertyAccessorCollectionTest
    5614303 [PropertyAccess] Refactored PropertyAccessorTest