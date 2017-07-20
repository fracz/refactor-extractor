commit 18d6b3c8dc9a123294c98e1814366fae17852a13
Author: Boone Gorges <boonebgorges@gmail.com>
Date:   Fri Sep 25 20:40:25 2015 +0000

    Force comment pagination on single posts.

    Previously, the 'page_comments' toggle allowed users to disable comment
    pagination. This toggle was only superficial, however. Even with
    'page_comments' turned on, `comments_template()` loaded all of a post's
    comments into memory, and passed them to `wp_list_comments()` and
    `Walker_Comment`, the latter of which produced markup for only the
    current page of comments. In other words, it was possible to enable
    'page_comments', thereby showing only a subset of a post's comments on a given
    page, but all comments continued to be loaded in the background. This technique
    scaled poorly. Posts with hundreds or thousands of comments would load slowly,
    or not at all, even when the 'comments_per_page' setting was set to a
    reasonable number.

    Recent changesets have addressed this problem through more efficient tree-
    walking, better descendant caching, and more selective queries for top-level
    post comments. The current changeset completes the project by addressing the
    root issue: that loading a post causes all of its comments to be loaded too.

    Here's the breakdown:

    * Comment pagination is now forced. Setting 'page_comments' to false leads to evil things when you have many comments. If you want to avoid pagination, set 'comments_per_page' to something high.
    * The 'page_comments' setting has been expunged from options-discussion.php, and from places in the codebase where it was referenced. For plugins relying on 'page_comments', we now force the value to `true` with a `pre_option` filter.
    * `comments_template()` now queries for an appropriately small number of comments. Usually, this means the `comments_per_page` value.
    * To preserve the current (odd) behavior for comment pagination links, some unholy hacks have been inserted into `comments_template()`. The ugliness is insulated in this function for backward compatibility and to minimize collateral damage. A side-effect is that, for certain settings of 'default_comments_page', up to 2x the value of `comments_per_page` might be fetched at a time.
    * In support of these changes, a `$format` parameter has been added to `WP_Comment::get_children()`. This param allows you to request a flattened array of comment children, suitable for feeding into `Walker_Comment`.
    * `WP_Query` loops are now informed about total available comment counts and comment pages by the `WP_Comment_Query` (`found_comments`, `max_num_pages`), instead of by `Walker_Comment`.

    Aside from radical performance improvements in the case of a post with many
    comments, this changeset fixes a bug that caused the first page of comments to
    be partial (`found_comments` % `comments_per_page`), rather than the last, as
    you'd expect.

    Props boonebgorges, wonderboymusic.
    Fixes #8071.
    Built from https://develop.svn.wordpress.org/trunk@34561


    git-svn-id: http://core.svn.wordpress.org/trunk@34525 1a063a9b-81f0-0310-95a4-ce76da25c4cd