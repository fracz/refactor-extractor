commit 990fa00f7e1a7a8d5bcda78681a9cd7df5d5c023
Author: Damien Martin-Guillerez <dmarting@google.com>
Date:   Thu Jun 2 12:18:11 2016 +0000

    Introduce FsEventsDiffAwareness for OS X.

    Use FsEvents API (https://developer.apple.com/library/mac/documentation/Darwin/Reference/FSEvents_Ref)
    to watch the file system. This change also refactor the LocalDiffAwareness to extract the
    WatchService specific part. It now select the FsEventsDiffAwareness on OSX and the
    WatchServiceDiffAwareness on Linux.

    RELNOTES[NEW]: On OSX, --watchfs now uses FsEvents to be notified of changes from the filesystem
                   (previously, this flag had no effect on OS X).

    Fixes #1074.

    --
    Change-Id: I927951468e4543a399e0e0ad0f1dd23d38ce15a0
    Reviewed-on: https://bazel-review.googlesource.com/3420
    MOS_MIGRATED_REVID=123854017