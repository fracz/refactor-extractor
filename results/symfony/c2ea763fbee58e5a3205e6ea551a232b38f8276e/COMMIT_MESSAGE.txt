commit c2ea763fbee58e5a3205e6ea551a232b38f8276e
Merge: 1c4c043 ec14143
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Fri Jun 5 15:50:22 2015 +0200

    minor #14880 [Console] Remove an unused argument (jakzal)

    This PR was merged into the 2.6 branch.

    Discussion
    ----------

    [Console] Remove an unused argument

    | Q             | A
    | ------------- | ---
    | Bug fix?      | no
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | #14878
    | License       | MIT
    | Doc PR        | -

    It was overlooked when doing a refactoring in #8800

    I also fixed a small cs issue indicated by fabbot.

    Commits
    -------

    ec14143 [Console] Remove an unused argument and fix a small cs issue