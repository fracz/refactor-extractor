commit 9ac40f1cf1125c6144e92518a9d44b0eea2ef7b7
Author: Fyodor Kupolov <fkupolov@google.com>
Date:   Tue Mar 28 19:11:17 2017 -0700

    Only use cacheLock when it's needed

    When reading from cache, we can avoid synchronization on dbLock if we
    only read from cache (no db access).

    When doing updates to db and cache, we should hold cacheLock only when
    updating the cache.

    This change improves locking in the following methods:
     - getAccountVisibilityFromCache
     - saveAuthTokenToDatabase
     - getAccountsFromCacheLocked no longer allows outside locking. The
       method was renamed to getAccountsFromCache and now self-manages locks
     - writeAuthTokenIntoCacheLocked
     - readAuthTokenInternal

    Test: AccountManagerServiceTest
    Bug: 36485175
    Bug: 35262596
    Change-Id: I9aca45c31716c4f0e0fd9f07859e88a7f5ba6922