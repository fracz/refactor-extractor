commit 8fd817b4d2ce59329ab9a341538939ad297e3aeb
Merge: 0c8eb2a d95c245
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Wed Jun 19 11:29:08 2013 +0200

    merged branch Strate/strate/improvement/get-parent-of-node (PR #8302)

    This PR was submitted for the 2.3 branch but it was merged into the master branch instead (closes #8302).

    Discussion
    ----------

    [Config] Allow to get parent of BaseNode

    | Q             | A
    | ------------- | ---
    | Bug fix?      | no
    | New feature?  | no (improvement)
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets |
    | License       | MIT
    | Doc PR        |

    Like an ArrayNode have method `getChildren`, I might to be good idea for having method `getParent` of all nodes.

    Commits
    -------

    a9a7ecc [Config] Allow to get parent of BaseNode