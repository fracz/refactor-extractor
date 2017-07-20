commit 31ef2305950a4c7c8d6f8e00771b5d21dd90819c
Author: Boone Gorges <boonebgorges@gmail.com>
Date:   Sun Jan 3 02:03:28 2016 +0000

    Ensure that non-default pagination values work in `wp_list_comments()`.

    Prior to 4.4, it was possible to pass 'page' and 'per_page' values to
    `wp_list_comments()` that do not match the corresponding global query vars.
    This ability was lost in 4.4 with the refactor of how `comments_template()`
    queries for comments; when the main comment query started fetching only the
    comments that ought to appear on a page, instead of all of a post's comments,
    it became impossible for the comment walker to select comments corresponding to
    custom pagination parameters. See #8071.

    We restore the previous behavior by (a) detecting when a 'page' or 'per_page'
    parameter has been passed to `wp_list_comments()` that does not match the
    corresponding query vars (so that the desired comments will not be found in
    `$wp_query`), and if so, then (b) querying for all of the post's comments and
    passing them to the comment walker for pagination, as was the case before 4.4.

    Props boonebgorges, smerriman.
    Fixes #35175.
    Built from https://develop.svn.wordpress.org/trunk@36157


    git-svn-id: http://core.svn.wordpress.org/trunk@36123 1a063a9b-81f0-0310-95a4-ce76da25c4cd