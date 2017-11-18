commit 17cae092da8903211e4a657c1d02c615c7b008d3
Author: Dan LaRocque <dalaro@hopcount.org>
Date:   Sun Sep 29 23:07:35 2013 -0400

    Restoring embedded Cassandra adapter

    This reverts commit f0c9468dd3e16779098faebc28f21c3fce834d4c.

    It revives the Cassandra embedded storage backend and its tests.  This
    is not a pure revert.  To make the code compile, I had to make a few
    changes atop the revert to account for refactorings in the interim.