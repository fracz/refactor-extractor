commit bef19d10b1262e610569b4f604635eb0bea07e01
Author: Dmitry Lomov <dslomov@google.com>
Date:   Thu Feb 16 17:00:59 2017 +0000

    Rollback of commit 8fb311b4dced234b2f799c16c7d08148619f4087.

    *** Reason for rollback ***

    Bisected by dmarting as a root cause of TF breakage: http://ci.bazel.io/job/TensorFlow/672/BAZEL_VERSION=HEAD,PLATFORM_NAME=linux-x86_64/console

    *** Original change description ***

    Reinstate idleness checks where the server self-terminates when it's idle and there is either too much memory pressure or the workspace directory is gone.

    Arguably, it should kill itself when the workspace directory is gone regardless of whether it's idle or not, but let's first get us back to a known good state, then we can think about improvements.

    --
    PiperOrigin-RevId: 147726386
    MOS_MIGRATED_REVID=147726386