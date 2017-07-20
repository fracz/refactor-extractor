commit cf7e00ed9fa55d24cacd413be76d3b17051ced0e
Author: Boone Gorges <boonebgorges@gmail.com>
Date:   Wed Jun 17 01:48:28 2015 +0000

    Performance enhancements for `_split_shared_term()`.

    * Introduce a `$record` parameter, which defaults to true. When set to false, `_split_shared_term()` will not keep a record of split term data in wp_options. The judicious use of this flag can greatly improve performance when processing shared terms in batches.
    * Allow term/tt objects to be passed to the `$term_id` and `$term_taxonomy_id` parameters. This has the potential to save database queries when the objects are already available.

    See #30261.
    Built from https://develop.svn.wordpress.org/trunk@32813


    git-svn-id: http://core.svn.wordpress.org/trunk@32784 1a063a9b-81f0-0310-95a4-ce76da25c4cd