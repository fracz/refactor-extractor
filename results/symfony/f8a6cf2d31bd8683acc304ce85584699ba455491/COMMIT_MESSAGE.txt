commit f8a6cf2d31bd8683acc304ce85584699ba455491
Merge: 566ad10 2568432
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Wed Oct 3 08:13:50 2012 +0200

    merged branch bschussek/issue5578 (PR #5655)

    This PR was merged into the 2.1 branch.

    Commits
    -------

    2568432 [Form] Hardened code of ViolationMapper against errors

    Discussion
    ----------

    [Form] Hardened code of ViolationMapper against errors

    Bug fix: no
    Feature addition: no
    Backwards compatibility break: no
    Symfony2 tests pass: yes
    Fixes the following tickets: -
    Todo: -
    License of the code: MIT
    Documentation PR: -

    This ticket improves the code of ViolationMapper to reduce the risk of bugs and in order to make further bug fixing easier. It was implemented while trying to solve #5578 and is semantically equivalent to the previous version.