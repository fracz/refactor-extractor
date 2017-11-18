commit 110484e6b1b8b60cf27ca53355dd7c690c308e11
Author: buchgr <buchgr@google.com>
Date:   Fri Jul 7 07:50:56 2017 -0400

    remote: Add a SingleSourceBuilder to the Chunker.

    Prepare the Chunker for an upcoming refactoring, where it will no longer
    support multiple input sources and digest filtering.

    RELNOTES: None.
    PiperOrigin-RevId: 161189759