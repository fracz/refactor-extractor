commit fe1dd1512643ef6b81e3c087989b3296f966a0ce
Author: Boone Gorges <boonebgorges@gmail.com>
Date:   Sat Feb 6 04:42:26 2016 +0000

    Allow `get_terms()` results to ordered by metadata.

    The `$orderby` parameter of `get_terms()` now accepts the following values,
    related to term meta:

    * 'meta_value'
    * 'meta_value_num'
    * the value of the `$meta_key` parameter
    * any key from the `$meta_query` array

    This brings order-by-meta support for terms in line with post, comment, and
    user queries.

    As a byproduct of these improvements, `$meta_key` and `$meta_value` parameters
    have been introduced to `get_terms()`. They interact with `$meta_query` in the
    same way as in `WP_Query` and other query classes.

    Props jadpm, eherman24.
    Fixes #34996.
    Built from https://develop.svn.wordpress.org/trunk@36485


    git-svn-id: http://core.svn.wordpress.org/trunk@36452 1a063a9b-81f0-0310-95a4-ce76da25c4cd