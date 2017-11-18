commit 8d739f2fad3fd0135b940170691b0119c5baca1e
Author: Philipp Wollermann <philwo@google.com>
Date:   Fri Mar 24 14:29:12 2017 +0000

    sandbox: Make /tmp and /dev/shm writable by default on Linux.

    Also refactor the way we compute writable dirs so that they're computed
    only once per running action, not twice.

    Fixes #2056, fixes #1973, fixes #1460.

    RELNOTES: /tmp and /dev/shm are now writable by default inside the
    Linux sandbox.

    --
    PiperOrigin-RevId: 151123543
    MOS_MIGRATED_REVID=151123543