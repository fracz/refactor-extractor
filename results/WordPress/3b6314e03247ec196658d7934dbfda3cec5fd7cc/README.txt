commit 3b6314e03247ec196658d7934dbfda3cec5fd7cc
Author: Boone Gorges <boonebgorges@gmail.com>
Date:   Wed Sep 28 03:55:33 2016 +0000

    Taxonomy: Use `WP_Term_Query` when querying for object terms.

    The new 'object_ids' parameter for `WP_Term_Query` allows queries for
    terms that "belong to" a given object. This change makes it possible
    to use `WP_Term_Query` inside of `wp_get_object_terms()`, rather than
    assembling a SQL query.

    The refactor has a couple of benefits:
    * Less redundancy.
    * Better consistency in accepted arguments between the term query functions. See #31105.
    * Less redundancy.
    * Object term queries are now cached. The `get_object_term_cache()` cache remains, and will be a somewhat less fragile secondary cache in front of the query cache (which is subject to frequent invalidation).
    * Less redundancy.

    A small breaking change: Previously, if a non-hierarchical taxonomy had
    terms that had a non-zero 'parent' (perhaps because of a direct SQL
    query), `wp_get_object_terms()` would respect the 'parent' argument.
    This is in contrast to `WP_Term_Query` and `get_terms()`, which have
    always rejected 'parent' queries for non-hierarchical taxonomies. For
    consistency, the behavior of `get_terms()` is being applied across the
    board: passing 'parent' for a non-hierarchical taxonomy will result in
    an empty result set (since the cached taxonomy hierarchy will be empty).

    Props flixos90, boonebgorges.
    See #37198.
    Built from https://develop.svn.wordpress.org/trunk@38667


    git-svn-id: http://core.svn.wordpress.org/trunk@38610 1a063a9b-81f0-0310-95a4-ce76da25c4cd