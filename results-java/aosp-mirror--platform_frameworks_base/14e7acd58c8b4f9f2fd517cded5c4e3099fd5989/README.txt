commit 14e7acd58c8b4f9f2fd517cded5c4e3099fd5989
Author: David Brazdil <dbrazdil@google.com>
Date:   Wed Apr 27 10:35:56 2016 +0100

    Fix infinite loop during package-usage.list file upgrade

    When upgrading from version 0 to version 1 of the file
    '/data/system/package-usage.list', the PackageManagerService can get
    stuck in an infinite loop if one of the listed packages does not
    exist, e.g. because it had been uninstalled. Fix the issue by
    refactoring the loop.

    Bug: 28409278
    Change-Id: Ia312bd0d04f696240445b710dd6a68b93c5d5946