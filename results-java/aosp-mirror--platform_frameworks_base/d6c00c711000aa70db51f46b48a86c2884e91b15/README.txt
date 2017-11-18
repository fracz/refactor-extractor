commit d6c00c711000aa70db51f46b48a86c2884e91b15
Author: Artem Iglikov <artikz@google.com>
Date:   Mon Apr 24 12:03:42 2017 +0100

    Encapsulate RefactoredBackupManagerService fields.

    This encapsulates back the fields which were decapsulated when splitting
    out internal classes.

    Bug: 37520921
    Test: adb shell settings put global backup_refactored_service_disabled 0
    && adb reboot && adb shell bmgr backupnow --all
    Change-Id: I9caa75b2f688de96bd5b245f43e0ae66cd9d023c