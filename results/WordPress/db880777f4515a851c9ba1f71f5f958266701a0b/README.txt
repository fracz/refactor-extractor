commit db880777f4515a851c9ba1f71f5f958266701a0b
Author: Boone Gorges <boonebgorges@gmail.com>
Date:   Tue Sep 29 22:00:24 2015 +0000

    Improve lazyloading of term metadata in `WP_Query` loops.

    [34529] introduced lazyloading for the metadata belonging to terms matching
    posts in the main `WP_Query`. The current changeset improves this technique
    in the following ways:

    * Term meta lazyloading is now performed on the results of all `WP_Query` queries, not just the main query.
    * Fewer global variable touches and greater encapsulation.
    * The logic for looping through posts to identify terms is now only performed once per `WP_Query`.

    Props dlh, boonebgorges.
    See #34047.
    Built from https://develop.svn.wordpress.org/trunk@34704


    git-svn-id: http://core.svn.wordpress.org/trunk@34668 1a063a9b-81f0-0310-95a4-ce76da25c4cd