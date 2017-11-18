commit 8ad028117d4b99883bbc52b29f097b2fb1d9b0c2
Author: Joe Onorato <joeo@android.com>
Date:   Wed May 13 01:41:44 2009 -0400

    With this, the BackupService onBackup method is called.

    It took a bunch of refactoring inside BackupManagerService,
    which is unfortunately all temporary anyway, but it unblocks
    a bunch of stuff.