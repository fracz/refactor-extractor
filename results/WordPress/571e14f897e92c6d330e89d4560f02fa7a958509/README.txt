commit 571e14f897e92c6d330e89d4560f02fa7a958509
Author: Boone Gorges <boonebgorges@gmail.com>
Date:   Wed Feb 17 22:58:26 2016 +0000

    More performance improvements to metadata lazyloading.

    Comment and term meta lazyloading for `WP_Query` loops, introduced in 4.4,
    depended on filter callback methods belonging to `WP_Query` objects. This meant
    storing `WP_Query` objects in the `$wp_filter` global (via `add_filter()`),
    requiring that PHP retain the objects in memory, even when the local variables
    would typically be expunged during normal garbage collection. In cases where a
    large number of `WP_Query` objects were instantiated on a single pageload,
    and/or where the contents of the `WP_Query` objects were quite large, serious
    performance issues could result.

    We skirt this problem by moving metadata lazyloading out of `WP_Query`. The
    new `WP_Metadata_Lazyloader` class acts as a lazyload queue. Query instances
    register items whose metadata should be lazyloaded - such as post terms, or
    comments - and a `WP_Metadata_Lazyloader` method will intercept comment and
    term meta requests to perform the cache priming. Since `WP_Metadata_Lazyloader`
    instances are far smaller than `WP_Query` (containing only object IDs), and
    clean up after themselves far better than the previous `WP_Query` methods (bp
    only running their callbacks a single time for a given set of queued objects),
    the resource use is decreased dramatically.

    See [36525] for an earlier step in this direction.

    Props lpawlik, stevegrunwell, boonebgorges.
    Fixes #35816.
    Built from https://develop.svn.wordpress.org/trunk@36566


    git-svn-id: http://core.svn.wordpress.org/trunk@36533 1a063a9b-81f0-0310-95a4-ce76da25c4cd