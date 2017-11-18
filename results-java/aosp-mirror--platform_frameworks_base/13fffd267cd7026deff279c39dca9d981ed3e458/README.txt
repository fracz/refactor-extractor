commit 13fffd267cd7026deff279c39dca9d981ed3e458
Author: Artem Iglikov <artikz@google.com>
Date:   Mon May 8 17:17:13 2017 +0100

    Add some tests for TarBackupReader

    ... and PerformAdbRestoreTask.

    Involves some refactoring, splitting readAppManifest() into two methods.

    Also a bit of cleanup: make private field actually private and use
    static imports for some constants.

    Bug: 38090803
    Bug: 37619463
    Test: runtest -p com.android.server.backup frameworks-services
    Change-Id: Ic30a6c5a515da1efb67caaae6eb75f4313797d5c