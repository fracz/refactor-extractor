commit 1f2635d52b34c2a3df967135a20c12f415299103
Author: Boone Gorges <boonebgorges@gmail.com>
Date:   Wed Jun 1 21:26:27 2016 +0000

    Make the 'comment' cache group persistent.

    'comment' was made non-persistent in [7986], to address the difficulty of
    reliable cache invalidation. Since then, the comment system has improved such
    that we can be more confident that caches are being busted as needed.

    Props spacedmonkey.
    Fixes #36906.
    Built from https://develop.svn.wordpress.org/trunk@37613


    git-svn-id: http://core.svn.wordpress.org/trunk@37581 1a063a9b-81f0-0310-95a4-ce76da25c4cd