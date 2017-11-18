commit 53d74f9556616661ddae20765a508dec61f785b2
Author: olaola <olaola@google.com>
Date:   Mon Jul 24 12:02:27 2017 +0200

    Tiny refactoring, functional no-op.

    After the ByteUploader changes, the Retrier no longer needs the onFailure(s) functions. Removing them will simplify both the code and the stack traces used for debugging problems.

    TESTED=unit tests
    RELNOTES: none
    PiperOrigin-RevId: 162913762