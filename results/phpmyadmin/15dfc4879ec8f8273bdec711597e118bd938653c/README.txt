commit 15dfc4879ec8f8273bdec711597e118bd938653c
Author: Michal Čihař <mcihar@novell.com>
Date:   Mon Jul 26 13:29:29 2010 +0200

    Drop collation description cache.

    It really does not improve performance much and it just consumes memory.
    Anyway on most pages this function is called just once for each
    collation, so all it does is filling up the cache but never using it.