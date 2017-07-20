commit 67d36f845d0df3f7e9bde7ae2fc4b94bb4b8af28
Author: Boone Gorges <boonebgorges@gmail.com>
Date:   Tue Jan 27 20:04:23 2015 +0000

    Improve organiation of tax_query and meta_query unit tests.

    `meta_query` tests have been moved to `tests/phpunit/tests/query/metaQuery.php` and `tax_query` tests to `tests/phpunit/tests/query/taxQuery.php`. This is an improvement because (a) it better corresponds to the way that other `WP_Query` parameter tests are organized, (b) splitting meta and tax tests into separate classes simplifies the required `@group` annotations, and (c) the tests have nothing to do with posts per se, and so do not belong in the `post` subdirectory.

    The tests previously found at `tests/phpunit/tests/query/taxQuery.php` have been moved to `isTerm.php` in the same directory. These tests are related to the `is_*` functions that have to do with taxonomy terms, like `is_category()`.

    See #26999.
    Built from https://develop.svn.wordpress.org/trunk@31286


    git-svn-id: http://core.svn.wordpress.org/trunk@31267 1a063a9b-81f0-0310-95a4-ce76da25c4cd