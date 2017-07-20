commit 29216371bd899e445e53fa6072ee622c424c0d31
Author: Boone Gorges <boonebgorges@gmail.com>
Date:   Tue May 5 21:59:26 2015 +0000

    In `paginate_links()`, improve handling of custom pagination query vars.

    Custom pagination query vars, as provided in the 'base' parameter, must be
    detected in the current page URL and removed before generating fresh pagination
    links. The logic introduced in this changeset ensures that these custom
    query vars are properly detected in cases where the 'format' param contains
    a `#`.

    This is a follow-up to [31203] #30831.

    Fixes #31939.
    Built from https://develop.svn.wordpress.org/trunk@32359


    git-svn-id: http://core.svn.wordpress.org/trunk@32330 1a063a9b-81f0-0310-95a4-ce76da25c4cd