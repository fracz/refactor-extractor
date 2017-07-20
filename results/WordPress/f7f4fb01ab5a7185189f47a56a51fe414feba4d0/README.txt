commit f7f4fb01ab5a7185189f47a56a51fe414feba4d0
Author: Boone Gorges <boonebgorges@gmail.com>
Date:   Mon Sep 28 19:30:26 2015 +0000

    Fix comment_order for single page comment threads.

    The old comment pagination logic had a separate block for comment threads that
    appeared on a single page. After the refactoring in [34561], all comment
    pagination logic is unified.

    This change ensures that 'comment_order' is respected in all scenarios.

    Fixes #8071.
    Built from https://develop.svn.wordpress.org/trunk@34669


    git-svn-id: http://core.svn.wordpress.org/trunk@34633 1a063a9b-81f0-0310-95a4-ce76da25c4cd