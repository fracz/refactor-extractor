commit 9626bb4923c74c6d3c09b7438eb24b32191053df
Author: buchgr <buchgr@google.com>
Date:   Fri Aug 11 13:54:04 2017 +0200

    remote: Refactor RemoteSpawnRunner to distinquish between remote
    executor, remote cache and local executor errors.

    This is a no-op refactoring and clears the way to integrate a change
    that no longer uploads to the remote cache if a previous remote cache
    interaction failed.

    RELNOTES: None.
    PiperOrigin-RevId: 164966432