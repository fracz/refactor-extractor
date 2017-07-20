commit 43e869b0bef33f73530e78a964a09f2ab7f264ad
Author: Mark Story <mark@mark-story.com>
Date:   Sun May 10 22:51:53 2015 -0400

    Instead of paths, use nested validator objects.

    @lorenzo made a great suggestion on how to improve nested validators.
    Instead of using paths, we can nest or mount validators inside of each
    other. With a few pre-checks this works really well.