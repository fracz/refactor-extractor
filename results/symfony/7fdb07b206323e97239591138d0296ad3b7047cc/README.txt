commit 7fdb07b206323e97239591138d0296ad3b7047cc
Merge: 56f3154 cd4349d
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Fri Jan 16 17:22:21 2015 +0100

    minor #13430 [Yaml] execute cheaper checks before more expensive ones (xabbuh)

    This PR was merged into the 2.3 branch.

    Discussion
    ----------

    [Yaml] execute cheaper checks before more expensive ones

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

    Minor improvements to the checks as suggested by @stof in #13262.

    Commits
    -------

    cd4349d execute cheaper checks before more expensive ones