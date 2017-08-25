commit 06af2d035180c4fb746a9b88c11178c516c88092
Author: Michael Hamann <michael@content-space.de>
Date:   Mon Nov 15 22:08:06 2010 +0100

    Indexer speed improvement: joined array vs. single lines

    From my experience with a benchmark of the indexer it is faster to first
    join the array of all index entries and then write them back together
    instead of writing every single entry. This might increase memory usage,
    but I couldn't see a significant increase and this function is also only
    used for the small index files, not for the large pagewords index.