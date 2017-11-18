commit 5d2ae52c032203575f145ca409f2b86f1e8c700b
Author: Ulf Adams <ulfjack@google.com>
Date:   Fri Feb 17 13:47:44 2017 +0000

    Move FileAccessException to the vfs package

    It was previously in unix, but also used from non-unix file systems, which
    means it's not actually unix-specific. This is in preparation for splitting
    compilation of the unix and windows file systems into separate libraries.

    That improves layering and reduces compile times - note that Bazel already
    injects the vfs into its lower layers, which should only rely on the normal
    vfs APIs, not on anything platform-specific.

    --
    PiperOrigin-RevId: 147829659
    MOS_MIGRATED_REVID=147829659