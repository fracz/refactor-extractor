commit 0eb3402795b8a854b52423aae7fd7b0bbbcde17a
Author: Robert Muir <rmuir@apache.org>
Date:   Sun Nov 9 03:17:26 2014 -0500

    Internal: harden recovery for old segments

    When a lucene 4.8+ file is transferred, Store returns a VerifyingIndexOutput
    that verifies both the CRC32 integrity and the length of the file.

    However, for older files, problems can make it to the lucene level. This is not great
    since older lucene files aren't especially strong as far as detecting issues here.

    For example, if a network transfer is closed on the remote side, we might write a
    truncated file... which old lucene formats may or may not detect.

    The idea here is to verify old files with their legacy Adler32 checksum, plus expected
    length. If they don't have an Adler32 (segments_N, jurassic elasticsearch?, its optional
    as far as the protocol goes), then at least check the length.

    We could improve it for segments_N, its had an embedded CRC32 forever in lucene, but this
    gets trickier. Long term, we should also try to also improve tests around here, especially
    backwards compat testing, we should test that detected corruptions are handled properly.

    Closes #8399

    Conflicts:
            src/main/java/org/elasticsearch/index/store/Store.java
            src/test/java/org/elasticsearch/index/store/StoreTest.java