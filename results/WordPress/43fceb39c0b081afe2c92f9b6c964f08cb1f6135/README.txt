commit 43fceb39c0b081afe2c92f9b6c964f08cb1f6135
Author: Boone Gorges <boonebgorges@gmail.com>
Date:   Sat Jan 24 18:48:23 2015 +0000

    Introduce 'childless' parameter to `get_terms()`.

    This new parameter allows developers to limit queried terms to terminal nodes -
    ie, those without any descendants.

    As part of the improvement, some internal logic in `get_terms()` has been
    consolidated. Parameters that resolve to a NOT IN clause containing term IDs
    ('exclude', 'exclude_tree', and 'childless') are now parsed into a single
    "exclusions" array before the SQL clause is generated.

    Props theMikeD, horike.
    Fixes #29839.
    Built from https://develop.svn.wordpress.org/trunk@31275


    git-svn-id: http://core.svn.wordpress.org/trunk@31256 1a063a9b-81f0-0310-95a4-ce76da25c4cd