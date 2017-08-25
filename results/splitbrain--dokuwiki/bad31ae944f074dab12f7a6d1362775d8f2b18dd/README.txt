commit bad31ae944f074dab12f7a6d1362775d8f2b18dd
Author: Andreas Gohr <andi@splitbrain.org>
Date:   Sat Oct 29 02:26:52 2005 +0200

    JavaScript refactoring

    This patch addes a first go on a central javascript and CSS dispatcher
    which builds a single script from all needed scripts, does optimizing
    and caching.

    darcs-hash:20051029002652-7ad00-7558b569c2bf65f5e41820644580d97c62edd0d6.gz