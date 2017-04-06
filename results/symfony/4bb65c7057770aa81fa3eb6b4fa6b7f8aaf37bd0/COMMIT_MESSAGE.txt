commit 4bb65c7057770aa81fa3eb6b4fa6b7f8aaf37bd0
Merge: c21c3bb dee47b1
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Thu Mar 8 23:40:15 2012 +0100

    merged branch drak/doctrinetest (PR #3531)

    Commits
    -------

    dee47b1 [DoctrineBridge] Add minimal tests for DBAL session storage driver

    Discussion
    ----------

    [2.1][DoctrineBridge] Add minimal tests for DBAL session storage driver

    Bug fix: no
    Feature addition: yes
    Backwards compatibility break: no
    Symfony2 tests pass: yes
    Fixes the following tickets: -
    Todo: -

    This is intentionally only for the `master` branch because the class is different between 2.0 and master.  This test is the minimal but at least will mean any refactoring changes in dependencies get caught.