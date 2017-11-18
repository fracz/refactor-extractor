commit 5f00cd2b1cde682f77a47439e9dc631349992f9e
Author: olaola <olaola@google.com>
Date:   Fri Jun 30 02:17:43 2017 +0200

    Slight refactoring, functional noop: uploadChunks will need to get a Chunker.Builder in my next change, so all the from factory methods are removed.

    TESTED=unit test
    PiperOrigin-RevId: 160594730