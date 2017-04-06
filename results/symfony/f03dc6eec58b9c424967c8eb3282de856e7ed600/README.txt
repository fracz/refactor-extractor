commit f03dc6eec58b9c424967c8eb3282de856e7ed600
Merge: 2f2ce3e b3da6a1
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Thu Mar 31 11:59:59 2016 +0200

    minor #18257 [Routing] Don't needlessly execute strtr's as they are fairly expensive (arjenm)

    This PR was merged into the 2.7 branch.

    Discussion
    ----------

    [Routing] Don't needlessly execute strtr's as they are fairly expensive

    | Q             | A
    | ------------- | ---
    | Branch?       | 2.7
    | Bug fix?      | refactor
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | see discussion in #18230
    | License       | MIT
    | Doc PR        | see #18230

    As requested in #18230 this is a new version of the prevention of using strtr's. I've posted some performance-numbers in that PR as well.

    Commits
    -------

    b3da6a1 [Routing] Don't needlessly execute strtr's as they are fairly expensive