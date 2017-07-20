commit 1877b8c726723ba6400782cd346178b5be5130ad
Author: Boone Gorges <boonebgorges@gmail.com>
Date:   Fri Feb 12 04:10:25 2016 +0000

    Set the `$comment` global in `comment_form_title()`.

    In [33963], `comment_form_title()` was refactored so that it no longer made
    reference to the `$comment` global. This broke some functionality within the
    comment form, as certain template would no longer be able to access the
    "current" comment.

    Props d4z_c0nf, WisdmLabs, boonebgorges.
    Fixes #35624.
    Built from https://develop.svn.wordpress.org/trunk@36512


    git-svn-id: http://core.svn.wordpress.org/trunk@36479 1a063a9b-81f0-0310-95a4-ce76da25c4cd