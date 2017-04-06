commit 0e2370e65e273eeef9125f0a880965a66b132fe1
Merge: d63a317 76e5bce
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Mon Dec 10 13:43:10 2012 +0100

    merged branch Tobion/patch-1 (PR #6237)

    This PR was merged into the master branch.

    Commits
    -------

    76e5bce no need to set the compiled route to null when cloning

    Discussion
    ----------

    [Routing] no need to set the compiled route to null when cloning

    The compiled reference can be reused when cloning. When the route is changed, the compiled reference is set to null anyway. So if you just clone the route, this improves performance as it does not need to recompile.