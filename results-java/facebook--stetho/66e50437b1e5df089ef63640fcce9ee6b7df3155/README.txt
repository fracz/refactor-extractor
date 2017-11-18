commit 66e50437b1e5df089ef63640fcce9ee6b7df3155
Author: Josh Guilfoyle <jasta@devtcg.org>
Date:   Fri Apr 3 10:30:39 2015 -0700

    Fetch deep links to find large format image URLs for APOD sample

    This adds somewhat arbitrary network overhead to highlight the kinds of
    bugs/inefficiencies that Stetho is good at rooting out.  Specifically in
    this case, we improved the quality of the APOD thumbnail image but by
    greatly overfetching data and slowing down the UI.  Stetho's Network tab
    makes it very easy to see why (adding an extra round-trip through an
    HTML page and downloading images that are 4x+ larger than the space they
    occupy)