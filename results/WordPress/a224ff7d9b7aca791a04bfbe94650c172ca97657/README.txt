commit a224ff7d9b7aca791a04bfbe94650c172ca97657
Author: Boone Gorges <boonebgorges@gmail.com>
Date:   Mon Feb 16 14:10:27 2015 +0000

    Improve 'orderby' syntax for `WP_Comment_Query`.

    Since [29027], `WP_Query` has supported an array of values for the `$orderby`
    parameter, with field names as array keys and ASC/DESC as the array values.
    This changeset introduces the same syntax to `WP_Comment_Query`.

    We leverage the new support for multiple ORDER BY clauses to fix a bug that
    causes comments to be queried in an indeterminate order when sorting by the
    default `comment_date_gmt` and comments share the same value for
    `comment_date_gmt`. By always including a `comment_ID` subclause at the end of
    the ORDER BY statement, we ensure that comments always have a unique fallback
    for sorting.

    This changeset also includes improvements paralleling those introduced to
    `WP_Query` in [31312] and [31340], which allow `$orderby` to accept array keys
    from specific `$meta_query` clauses. This change lets devs sort by multiple
    clauses of an associated meta query. See #31045.

    Fixes #30478. See #31265.
    Built from https://develop.svn.wordpress.org/trunk@31467


    git-svn-id: http://core.svn.wordpress.org/trunk@31448 1a063a9b-81f0-0310-95a4-ce76da25c4cd