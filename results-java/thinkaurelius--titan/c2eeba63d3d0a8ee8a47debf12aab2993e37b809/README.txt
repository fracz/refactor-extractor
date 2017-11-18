commit c2eeba63d3d0a8ee8a47debf12aab2993e37b809
Author: Dan LaRocque <dalaro@hopcount.org>
Date:   Sun Jun 3 03:59:15 2012 -0400

    Clean up testing-related Cassandra config hack

    During EC2 testing earlier this week, I hard-coded a path string
    containing forward slashes and left a TODO comment on the same line.
    This is now refactored to use File.separator character like the old
    CassandraLocalhostHelper code.