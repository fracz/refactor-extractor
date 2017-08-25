commit 9f8c581f51b0ac5fa4230dee2a67712174f8df17
Author: Mat Whitney <mwhitney@mail.sdsu.edu>
Date:   Thu May 9 10:48:10 2013 -0700

    #808 Template library optimizations

    Cut the number of calls to find_files() nearly in half for most pages on
    my site and improved the performance of find_files() itself slightly
    (some sites with unusual configurations, i.e. multiple theme paths, may
    see a more significant improvement)