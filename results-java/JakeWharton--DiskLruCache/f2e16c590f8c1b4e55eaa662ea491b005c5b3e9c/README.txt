commit f2e16c590f8c1b4e55eaa662ea491b005c5b3e9c
Author: jwilson <jwilson@squareup.com>
Date:   Sun Dec 23 23:18:34 2012 -0500

    Performance improvement in DiskLruCache.readJournalLine().

    Original AOSP/libcore change by Vladimir Marko:
    Speed up DiskLruCache.readJournalLine() by avoiding memory
    allocations from String.split(). For non-CLEAN lines, we
    avoid using String.split() altogether and find separators
    explicitly, for CLEAN lines we defer to String.split() and
    we optimize the underlying Splitter.fastSplit() overload to
    avoid unnecessary allocations itself.

    On a test journal with 7347 entries (1099 CLEAN) this saves
    about 45-50% from ~250ms. On a test journal with 272 entries
    (86 CLEAN) this saves about 35-40% from ~10ms. Measured
    loadJournal on GN in a tight loop (file contents cached).

    If used without the other DiskLruCache.readJournalLine()
    improvements, the Splitter.fastSplit() optimization alone
    would provide about 60% of the savings. It should also
    speed up other code outside the DiskLruCache.

    Change-Id: I1d6c6b13d54d8fcba3081f2bb9df701b58f5e143