commit 65f086eb452cac1fc83cfac710303c5b020d0697
Author: lvca <l.garulli@gmail.com>
Date:   Mon Apr 25 18:46:13 2016 +0200

    LockManager: refactored both implementation with a unique interface. Using "old" lock manager when running distributed

    Also changed the name to be more clear instead of “ONewLockManager”.
    Fixed issue #5892.