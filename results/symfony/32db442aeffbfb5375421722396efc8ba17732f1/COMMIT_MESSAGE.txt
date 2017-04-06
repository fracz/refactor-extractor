commit 32db442aeffbfb5375421722396efc8ba17732f1
Merge: bb4d45e 71d84e6
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Wed Apr 15 17:26:47 2015 +0200

    bug #14338 [FrameworkBundle] improve usage of Table helper (xabbuh)

    This PR was merged into the 2.6 branch.

    Discussion
    ----------

    [FrameworkBundle] improve usage of Table helper

    | Q             | A
    | ------------- | ---
    | Bug fix?      | yes
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets |
    | License       | MIT
    | Doc PR        |

    Use the `Table` helper if present in favor of the deprecated
    `TableHelper` class.

    Commits
    -------

    71d84e6 [FrameworkBundle] improve usage of Table helper