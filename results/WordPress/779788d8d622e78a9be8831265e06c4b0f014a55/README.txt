commit 779788d8d622e78a9be8831265e06c4b0f014a55
Author: Boone Gorges <boonebgorges@gmail.com>
Date:   Fri Jun 23 02:06:42 2017 +0000

    When querying for terms, do not assume that `$taxonomies` is a 0-indexed array.

    In [25108], the logic of term queries was modified to avoid assuming
    that the `taxonomies` array was numerically indexed. See #23506. This
    fix was inadvertantly reverted during the refactor in [25162].

    Props david.binda.
    Fixes #41113.
    Built from https://develop.svn.wordpress.org/trunk@40924


    git-svn-id: http://core.svn.wordpress.org/trunk@40774 1a063a9b-81f0-0310-95a4-ce76da25c4cd