commit cbdaf4950bdf44b9669f0d1a169ac23c18eebab4
Author: Martijn van Groningen <martijn.v.groningen@gmail.com>
Date:   Thu Aug 15 10:58:40 2013 +0200

    Added percolator improvements:
    * The _percolator type now has always to _id field enabled (index=not_analyzed, store=no)
    * During loading shard initialization the query ids are fetched from field data, before ids were fetched from stored values.
    * Moved internal percolator query map storage from Text to HashedBytesRef based keys.