commit 5021918605020c44c7ee66fbc896955abdd1517f
Author: Artem Iglikov <artikz@google.com>
Date:   Wed Apr 5 15:27:54 2017 +0100

    Clone BackupManagerService and make Trampoline aware of the clone.

    RefactoredBackupManagerService is an exact clone of
    BackupManagerService. Trampoline chooses between these two based on
    backup_refactored_service_disabled global setting, defaulting to true.

    Test: manual: flashed the device, ran `settings put global
    backup_refactored_service_disabled 1|0`, verified that correct class was
    instantiated, ran bmgr backupnow command to make sure that it works.

    Bug: 36850431

    Change-Id: I8ef91b928a40aae022f88f07a4126a00b1d5e220