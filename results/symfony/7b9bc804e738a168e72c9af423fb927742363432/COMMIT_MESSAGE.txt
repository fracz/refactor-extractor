commit 7b9bc804e738a168e72c9af423fb927742363432
Merge: 19aa6dc 9cacecb
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Sat Feb 21 09:06:16 2015 +0100

    minor #13742 [PropertyAccess] refactor type checks (Tobion)

    This PR was merged into the 2.3 branch.

    Discussion
    ----------

    [PropertyAccess] refactor type checks

    | Q             | A
    | ------------- | ---
    | Bug fix?      | yes
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets |
    | License       | MIT
    | Doc PR        | -

    #13735 for 2.3

    Commits
    -------

    9cacecb [PropertyAccess] the property path constructor already implements the type check
    4e11c07 [PropertyAccess] refactor type checks to remove duplicate logic