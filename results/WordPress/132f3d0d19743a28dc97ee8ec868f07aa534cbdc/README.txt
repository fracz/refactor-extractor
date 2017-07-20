commit 132f3d0d19743a28dc97ee8ec868f07aa534cbdc
Author: Boone Gorges <boonebgorges@gmail.com>
Date:   Fri Sep 9 19:49:29 2016 +0000

    Query: Eliminate unnecessary `wp_list_filter()` call in `get_queried_object()`.

    The refactor in [30711] swapped out the old `queries` property for the
    new `queried_terms`, but should also have gotten rid of the now-
    superfluous `wp_list_filter()` call.

    Fixes #37962.
    Built from https://develop.svn.wordpress.org/trunk@38586


    git-svn-id: http://core.svn.wordpress.org/trunk@38529 1a063a9b-81f0-0310-95a4-ce76da25c4cd