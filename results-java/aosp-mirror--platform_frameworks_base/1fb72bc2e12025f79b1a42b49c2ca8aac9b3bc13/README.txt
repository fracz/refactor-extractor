commit 1fb72bc2e12025f79b1a42b49c2ca8aac9b3bc13
Author: Artem Iglikov <artikz@google.com>
Date:   Mon Apr 3 14:08:35 2017 +0100

    Extract interface of BackupManagerService and make BackupManagerService implement this interface.

    This is part of (mostly) automated refactoring of BackupManagerService, see go/br-bm-automated-refactor.

    All done automatically with https://www.jetbrains.com/help/idea/2016.3/extract-interface.html

    Test: not required, as this doesn't change any behaviour.

    Bug: 36850431

    Change-Id: I1efa15b0f600536a9fd555bed18f2e86c3bcfecb