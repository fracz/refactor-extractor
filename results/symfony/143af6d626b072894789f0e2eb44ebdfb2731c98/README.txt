commit 143af6d626b072894789f0e2eb44ebdfb2731c98
Merge: eac8e3a 719e037
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Thu Oct 31 16:51:38 2013 +0100

    minor #9370 [FrameworkBundle] add test for #5135 and simplify routing container parameter resolution (Tobion)

    This PR was merged into the 2.2 branch.

    Discussion
    ----------

    [FrameworkBundle] add test for #5135 and simplify routing container parameter resolution

    | Q             | A
    | ------------- | ---
    | Bug fix?      | no
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Fixes   | #5135
    | Tests pass?   | yes
    | license?      | MIT

    1. [FrameworkBundle] added working test case for issue #5135
    2. [FrameworkBundle] fix routing container parameter exception message which did not really make sense and improve regex performance

    Commits
    -------

    719e037 [FrameworkBundle] fix routing container parameter exception message
    8513ac9 [Routing] added working test case for issue #5135