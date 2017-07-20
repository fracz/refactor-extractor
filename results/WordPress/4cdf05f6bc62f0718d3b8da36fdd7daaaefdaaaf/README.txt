commit 4cdf05f6bc62f0718d3b8da36fdd7daaaefdaaaf
Author: Boone Gorges <boonebgorges@gmail.com>
Date:   Tue May 5 19:37:53 2015 +0000

    Improve performance of `get_page_children()`.

    The new algorithm uses a hash table rather than function recursion, reducing
    complexity to O(N). On large numbers of pages, the performance improvement is
    several orders of magnitude.

    Props santagada, hailin, mihai.
    Fixes #10852.
    Built from https://develop.svn.wordpress.org/trunk@32355


    git-svn-id: http://core.svn.wordpress.org/trunk@32326 1a063a9b-81f0-0310-95a4-ce76da25c4cd