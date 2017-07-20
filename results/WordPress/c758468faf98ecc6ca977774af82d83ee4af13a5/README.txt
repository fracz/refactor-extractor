commit c758468faf98ecc6ca977774af82d83ee4af13a5
Author: Boone Gorges <boonebgorges@gmail.com>
Date:   Thu Oct 8 21:28:24 2015 +0000

    `WP_User_Query` role improvement redux.

    It's back, and it's better than ever: an overhaul of role-related arguments
    in `WP_User_Query`. This updated version of the previously-reverted [34875]
    includes support for the use of `$blog_id` without specifying a `$role`, for
    a 99.7% reduced chance of breaking wordpress.org and other large sites.

    Props boonebgorges, swissspidy.
    Fixes #22212.
    Built from https://develop.svn.wordpress.org/trunk@34959


    git-svn-id: http://core.svn.wordpress.org/trunk@34924 1a063a9b-81f0-0310-95a4-ce76da25c4cd