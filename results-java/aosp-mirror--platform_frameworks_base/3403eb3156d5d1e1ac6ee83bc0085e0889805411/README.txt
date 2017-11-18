commit 3403eb3156d5d1e1ac6ee83bc0085e0889805411
Author: Cary Clark <cary@android.com>
Date:   Wed Mar 3 10:05:16 2010 -0500

    refactor find state and scrolling

    Separate out state when find is up and is empty.
    Request a scroll when setting a match, rather than when drawing.
    Don't draw if there's no match.

    Companion fix in external/webkit and packages/apps/Browser

    http://b/2370069