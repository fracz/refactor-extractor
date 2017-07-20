commit 90544d66f26abf2f72be43b0808fe91806bca7f5
Author: Boone Gorges <boonebgorges@gmail.com>
Date:   Mon Sep 28 15:10:50 2015 +0000

    In `get_page_of_comment()`, use a true `COUNT` query.

    This improves memory usage by not compiling a list of comment IDs.

    See #34057.
    Built from https://develop.svn.wordpress.org/trunk@34661


    git-svn-id: http://core.svn.wordpress.org/trunk@34625 1a063a9b-81f0-0310-95a4-ce76da25c4cd