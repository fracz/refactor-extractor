commit b5b923c3471e533f4bf116dfe7b1106184384fe0
Author: Boone Gorges <boonebgorges@gmail.com>
Date:   Sat Nov 8 19:29:22 2014 +0000

    Share fixtures across 'canonical' automated tests.

    Sharing these fixtures results in a speed improvement of almost one minute per
    run of the test suite.

    My hope is that future WordPress developers will spend this extra minute with
    their loved ones, for life on this earth is short, my friends, and the moments
    you spend watching WP generate test data can never again be reclaimed from the
    grizzled clutches of Time, and none of us are really getting younger, I mean,
    geez, have you looked in the mirror lately, Gandalf?

    See #30017.
    Built from https://develop.svn.wordpress.org/trunk@30277


    git-svn-id: http://core.svn.wordpress.org/trunk@30277 1a063a9b-81f0-0310-95a4-ce76da25c4cd