commit 000928f9dc8c44457814170a9e8642ae157c0d51
Author: Ben Newman <bn@cs.stanford.edu>
Date:   Mon Jun 17 16:27:02 2013 -0400

    Use recast.parse and .print for require("vendor/constants").propagate.

    This removes the need to pass a callback, which is a nice improvement.