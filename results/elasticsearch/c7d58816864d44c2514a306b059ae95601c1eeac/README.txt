commit c7d58816864d44c2514a306b059ae95601c1eeac
Author: Shay Banon <kimchy@gmail.com>
Date:   Wed Aug 7 23:27:17 2013 +0200

    make sure we add the _uid as the first field in a doc
    this will improve early termination loading times, but requires potential improvements in Lucene in terms of decompression