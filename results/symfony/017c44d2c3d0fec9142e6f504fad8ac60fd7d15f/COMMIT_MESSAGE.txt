commit 017c44d2c3d0fec9142e6f504fad8ac60fd7d15f
Merge: 1efb0f0 9ac3556
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Sat Jul 20 09:33:53 2013 +0200

    merged branch GromNaN/2.3-di-dump-exception (PR #8524)

    This PR was merged into the 2.3 branch.

    Discussion
    ----------

    [DependencyInjection][2.3] Add exception for service name not dumpable in PHP

    Same as #8494 for branch 2.3 since the DI component has been refactored (bb797ee75591f8461dbcc6fdc8a50b5d3aa8fe5a, f1c2ab78af69d5cfa1db2c244dbf22e0303036f2)

    | Q             | A
    | ------------- | ---
    | Bug fix?      | no
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | #8485 #8030
    | License       | MIT
    | Doc PR        | n/a

    Throws an exception when the DIC is dumped to PHP, before generating invalid PHP.
    The regex comes from the PHP doc: http://www.php.net/manual/en/language.oop5.basic.php

    Commits
    -------

    9ac3556 [DependencyInjection] Add exception for service name not dumpable in PHP