commit fde32cc599cdc763efaea7c77742619d1cac9078
Author: Ryan Ernst <ryan@iernst.net>
Date:   Mon Dec 8 14:58:32 2014 -0800

    Stats: Add more fine grained memory stats from Lucene segment reader.

    This is a start to exposing memory stats improvements from Lucene 5.0.
    This adds the following categories of Lucene index pieces to index stats:
    * Terms
    * Stored fields
    * Term Vectors
    * Norms
    * Doc values