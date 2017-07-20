commit 4c91776f3b1afb646454ad14e1a81459123c5f0a
Author: Boone Gorges <boonebgorges@gmail.com>
Date:   Fri Jan 15 20:10:51 2016 +0000

    Respect all post-related filters in `WP_Comment_Query`.

    The refactor of `WP_Comment_Query`'s SQL generation in [34542] introduced a bug
    that caused only the last post-related filter to be respected in comment
    queries. In other words, if querying for comments using params
    `post_status=draft&post_author=3`, only the last-processed of these params
    would be respected. The current changeset fixes the logic so that these clauses
    don't overwrite each other.

    Props chriscct7.
    Fixes #35478.
    Built from https://develop.svn.wordpress.org/trunk@36326


    git-svn-id: http://core.svn.wordpress.org/trunk@36293 1a063a9b-81f0-0310-95a4-ce76da25c4cd