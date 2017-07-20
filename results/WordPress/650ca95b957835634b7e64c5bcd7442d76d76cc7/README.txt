commit 650ca95b957835634b7e64c5bcd7442d76d76cc7
Author: Boone Gorges <boonebgorges@gmail.com>
Date:   Mon Mar 16 11:16:28 2015 +0000

    In `wp_insert_term()`, allow a term with an existing name if a unique `$slug` has been provided.

    `wp_insert_term()` protects against the creation of terms with duplicate names
    at the same level of a taxonomy hierarchy. However, it's historically been
    possible to override this protection by explicitly providing a value of `$slug`
    that is unique at the hierarchy tier. This ability was broken in [31734], and
    the current changeset restores the original behavior.

    A number of unit tests are added and refactored in support of these changes.

    See #17689 for discussion of a fix that was superceded by [31734]. This commit
    retains the fix for the underlying bug described in that ticket.

    See #31328.
    Built from https://develop.svn.wordpress.org/trunk@31792


    git-svn-id: http://core.svn.wordpress.org/trunk@31774 1a063a9b-81f0-0310-95a4-ce76da25c4cd