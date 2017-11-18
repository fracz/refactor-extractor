commit 5e89ae29f0e8bf71d54360d35243da905892b168
Author: Vasu Nori <vnori@google.com>
Date:   Wed Sep 15 14:23:29 2010 -0700

    fix compiled statement bugs, synchronization bugs

    1. when LRU cache wants to remove the eldest entry, it may be finalizing
    a statement that is still in use
    2. a couple of synchronization issues.
    3. a bit code refactoring in SQLiteCompiledSql
    4. remove a bunch of unsed debug code from SQLiteDatabase

    Change-Id: I45454dc2dbadd7d8842ba77dd2b0e9ff138ec6f4