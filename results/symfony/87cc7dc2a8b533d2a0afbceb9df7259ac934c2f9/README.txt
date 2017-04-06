commit 87cc7dc2a8b533d2a0afbceb9df7259ac934c2f9
Merge: c852586 f1c2ab7
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Mon May 6 08:21:51 2013 +0200

    merged branch Seldaek/methodmap (PR #7933)

    This PR was merged into the master branch.

    Discussion
    ----------

    [DependencyInjection] Add a method map to avoid computing method names from service names

    /cc @schmittjoh @stof

    The diff is a bit messy because of indenting, but all this adds really is an `if(isset($this->methodMap[$id])) { $method = $this->methodMap[$id]; }` that bypasses the method_exists + strtr calls. It's not a huge improvements but saves some cycles on something that's typically called a few hundred times per request.

    Commits
    -------

    f1c2ab7 [DependencyInjection] Add a method map to avoid computing method names from service names