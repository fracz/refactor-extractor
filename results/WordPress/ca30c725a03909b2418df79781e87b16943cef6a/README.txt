commit ca30c725a03909b2418df79781e87b16943cef6a
Author: Boone Gorges <boonebgorges@gmail.com>
Date:   Sat Jan 31 15:48:24 2015 +0000

    Improve support for ordering `WP_Query` results by postmeta.

    `WP_Meta_Query` clauses now support a 'name' parameter. When building a
    `WP_Query` object, the value of 'orderby' can reference this 'name', so that
    it's possible to order by any clause in a meta_query, not just the first one
    (as when using 'orderby=meta_value'). This improvement also makes it possible
    to order by multiple meta query clauses (or by any other eligible field plus
    a meta query clause), using the array syntax for 'orderby' introduced in [29027].

    Props Funkatronic, boonebgorges.
    Fixes #31045.
    Built from https://develop.svn.wordpress.org/trunk@31312


    git-svn-id: http://core.svn.wordpress.org/trunk@31293 1a063a9b-81f0-0310-95a4-ce76da25c4cd