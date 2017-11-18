commit 09ed8f6cfc3662d62d84fc65288408917906fa11
Author: Adrian Roos <roosa@google.com>
Date:   Thu Feb 16 00:55:42 2017 +0100

    SystemUI: Disable LeakReporterTest

    Those tests are slow because they dump the
    heap multiple times.

    Disable until the code is refactored such that
    it does not actually dump and write to disk
    when under test.

    Test: runtest systemui
    Bug: 35398375
    Change-Id: I7cf945687900424dc1460367fbef5f36ed75da82