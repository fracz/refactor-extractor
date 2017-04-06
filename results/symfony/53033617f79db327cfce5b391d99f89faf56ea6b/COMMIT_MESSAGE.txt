commit 53033617f79db327cfce5b391d99f89faf56ea6b
Merge: d2e5c4c a46270a
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Sun Mar 6 11:24:23 2016 +0100

    minor #18021 Don't use reflections when possible (Ener-Getick)

    This PR was merged into the 2.3 branch.

    Discussion
    ----------

    Don't use reflections when possible

    | Q             | A
    | ------------- | ---
    | Bug fix?      | no
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets |
    | License       | MIT
    | Doc PR        |

    Use php functions instead of reflection when possible (to improve a bit the performance).

    Commits
    -------

    a46270a Don't use reflections when possible