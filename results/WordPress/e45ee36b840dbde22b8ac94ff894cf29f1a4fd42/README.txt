commit e45ee36b840dbde22b8ac94ff894cf29f1a4fd42
Author: Boone Gorges <boonebgorges@gmail.com>
Date:   Fri Apr 21 19:14:44 2017 +0000

    Restore support for taxonomy 'args' override when querying object terms.

    [7520] introduced an undocumented feature whereby developers could
    register a custom taxonomy with an 'args' parameter, consisting of
    an array of config params that, when present, override corresponding
    params in the `$args` array passed to `wp_get_object_terms()` when
    using that function to query for terms in the specified taxonomy.

    The `wp_get_object_terms()` refactor in [38667] failed to respect
    this secret covenant, and the current changeset atones for the
    transgression.

    Props danielbachhuber.
    Fixes #40496.
    Built from https://develop.svn.wordpress.org/trunk@40513


    git-svn-id: http://core.svn.wordpress.org/trunk@40389 1a063a9b-81f0-0310-95a4-ce76da25c4cd