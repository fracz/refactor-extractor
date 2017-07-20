commit 9f36a943b5eae30e97d92df5b8e2dd783da4af18
Author: Boone Gorges <boonebgorges@gmail.com>
Date:   Sat Sep 26 16:02:25 2015 +0000

    Improve post field lazyloading for comments.

    [34583] modified comment queries so that all post fields are no longer loaded
    by default. Instead, they are loaded only when requested on individual comment
    objects. This changeset improves that flow:

    * `WP_Comment` magic methods `__isset()` and `__get()` should only load the post when a post field is being requested.
    * The new `update_comment_post_cache` argument for `WP_Comment_Query` allows developers to specify that, when comments are queried, all of the posts matching those comments should be loaded into cache with a single DB hit. This parameter defaults to false, since typical comment queries are linked to a single post.

    Fixes #27571.
    Built from https://develop.svn.wordpress.org/trunk@34599


    git-svn-id: http://core.svn.wordpress.org/trunk@34563 1a063a9b-81f0-0310-95a4-ce76da25c4cd