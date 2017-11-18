commit 3d73499a093f2fe950fee8fe788cc991002a80fd
Author: Fyodor Kupolov <fkupolov@google.com>
Date:   Wed Mar 29 17:28:52 2017 -0700

    Optimized locking for get/setUserData

    This change improves locking in the following methods:
     - setUserdata. cacheLock is only held when calling
       writeUserDataIntoCacheLocked
     - readUserData. dbLock is obtained only if not cached

    Test: AccountManagerServiceTest
    Bug: 36485175
    Bug: 35262596
    Change-Id: I65b939acedd69e3113c24b7e6788c7aefc6ba25a