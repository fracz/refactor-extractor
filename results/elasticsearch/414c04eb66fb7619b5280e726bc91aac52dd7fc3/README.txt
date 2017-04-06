commit 414c04eb66fb7619b5280e726bc91aac52dd7fc3
Author: Simon Willnauer <simonw@apache.org>
Date:   Fri Dec 4 10:08:11 2015 +0100

    Restore chunksize of 512kb on recovery and remove configurability

    This commit restores the chunk size of 512kb lost in a previous but unreleased
    refactoring. At the same time it removes the configurability of:
     * `indices.recovery.file_chunk_size` - now fixed to 512kb
     * `indices.recovery.translog_ops` - removed without replacement
     * `indices.recovery.translog_size` - now fixed to 512kb
     * `indices.recovery.compress` - file chunks are not compressed due to lucene's compression but translog operations are.

    The compress option is gone entirely and compression is used where it makes sense. On sending files of the index
    we don't compress as we rely on the lucene compression for stored fields etc.

    Relates to #15161