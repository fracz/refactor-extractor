commit 72e14046c785f9fc15c55fa21dcb695da1cac29b
Author: Boone Gorges <boonebgorges@gmail.com>
Date:   Thu Mar 16 02:04:43 2017 +0000

    Improve querying for terms with falsey names and slugs.

    Prior to [38677], `get_term_by()` would always return false if
    an empty string were passed as the queried 'name' or 'slug'. The
    refactor to use `get_terms()` broke this behavior; inappropriately
    imprecise `empty()` checks caused the 'name' or 'slug' clause to be
    discarded altogether when fetching terms, resulting in an incorrect
    term being returned from the function.

    We fix the regression by special-casing truly empty values passed
    to `get_term_by()`, and ensuring that `WP_Term_Query` is properly
    able to handle `0` and `'0'` term queries.

    Props sstoqnov.
    Fixes #21760.
    Built from https://develop.svn.wordpress.org/trunk@40293


    git-svn-id: http://core.svn.wordpress.org/trunk@40200 1a063a9b-81f0-0310-95a4-ce76da25c4cd