commit a50aca04e4c309632c8dc9d7ddf940a3debb4da3
Merge: cd0309f dee1562
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Fri Jun 20 09:17:53 2014 +0200

    bug #11168 [YAML] fix merge node (<<) (Tobion)

    This PR was merged into the 2.5 branch.

    Discussion
    ----------

    [YAML] fix merge node (<<)

    | Q             | A
    | ------------- | ---
    | Bug fix?      | yes
    | New feature?  | no
    | BC breaks?    | yes but according to spec
    | Deprecations? | no
    | Tests pass?   | yes
    | Needs merge to | 2.5
    | Fixed tickets | #11142 and #11154
    | License       | MIT
    | Doc PR        | —

    First commit small refactoring.
    Second fixes #11154 (causing a BC break for a less common feature)
    Third fixes #11142

    Commits
    -------

    dee1562 [Yaml] fix overwriting of keys after merged map
    8c621ab [Yaml] fix priority of sequence merges according to spec
    02614e0 [Yaml] refactoring of merges for performance