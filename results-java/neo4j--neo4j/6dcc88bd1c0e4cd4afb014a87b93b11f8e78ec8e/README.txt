commit 6dcc88bd1c0e4cd4afb014a87b93b11f8e78ec8e
Author: Mattias Persson <mattias@neotechnology.com>
Date:   Tue Apr 11 12:31:32 2017 +0200

    Flushes page cache sequentially during some import stages

    To get maximum sequential write I/O. Node and Relationship stages gets
    a throughput improvement up to 2x or more, depending on hardware.