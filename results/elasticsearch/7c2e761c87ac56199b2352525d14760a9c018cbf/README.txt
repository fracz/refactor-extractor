commit 7c2e761c87ac56199b2352525d14760a9c018cbf
Author: Ali Beyad <ali@elastic.co>
Date:   Wed Oct 12 12:38:26 2016 -0400

    Sequence numbers commit data in Lucene uses Iterable interface (#20793)

    Sequence number related data (maximum sequence number, local checkpoint,
    and global checkpoint) gets stored in Lucene on each commit. The logical
    place to store this data is on each Lucene commit's user commit data
    structure (see IndexWriter#setCommitData and the new version
    IndexWriter#setLiveCommitData). However, previously we did not store the
    maximum sequence number in the commit data because the commit data got
    copied over before the Lucene IndexWriter flushed the documents to segments
    in the commit.  This means that between the time that the commit data was
    set on the IndexWriter and the time that the IndexWriter completes the commit,
    documents with higher sequence numbers could have entered the commit.
    Hence, we would use FieldStats on the _seq_no field in the documents to get
    the maximum sequence number value, but this suffers the drawback that if the
    last sequence number in the commit corresponded to a delete document action,
    that sequence number would not show up in FieldStats as there would be no
    corresponding document in Lucene.

    In Lucene 6.2, the commit data was changed to take an Iterable interface, so
    that the commit data can be calculated and retrieved *after* all documents
    have been flushed, while the commit data itself is being set on the Lucene commit.
    This commit changes max_seq_no so it is stored in the commit data instead of
    being calculated from FieldStats, taking advantage of the deferred calculation
    of the max_seq_no through passing an Iterable that dynamically sets the iterator
    data.

    * improvements to iterating over commit data (and better safety guarantees)

    * Adds sequence number and checkpoint testing for document deletion
    intertwined with document indexing.

    * improve test code slightly

    * Remove caching of max_seq_no in commit data iterator and inline logging

    * Adds a test for concurrently indexing and committing segments
    to Lucene, ensuring the sequence number related commit data
    in each Lucene commit point matches the invariants of
    localCheckpoint <= highest sequence number in commit <= maxSeqNo

    * fix comments

    * addresses code review

    * adds clarification on checking commit data on recovery from translog

    * remove unneeded method