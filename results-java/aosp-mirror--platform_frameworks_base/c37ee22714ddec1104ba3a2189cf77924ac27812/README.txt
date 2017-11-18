commit c37ee22714ddec1104ba3a2189cf77924ac27812
Author: Carlos Valdivia <carlosvaldivia@google.com>
Date:   Wed Jun 17 20:17:37 2015 -0700

    Tweak GET_ACCOUNTS behavior and improve memory.

    Related to recent permissions and system health changes. This change
    will make it so that calls to AccountManager#getAccountsByType will work
    for the owning account authenticator even if they don't have
    permissions. This is pretty fundamental to having a working
    authenticator and it doesn't make sense to have it be disabled (or have
    authenticators hack around the framework).

    Also changed how TokenCache works so that memory usage is still
    predictable (no more than 64kb) but token caching won't be at the mercy
    of garbage collection. This is important for writing stable cts tests.

    Change-Id: Ib31b550616b266ee5a04eb26b04ba0023ca0cb83