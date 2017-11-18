commit 4869c4e17d5b1410070a1570f3244148d8f97b5d
Author: kush <kush@google.com>
Date:   Mon Oct 16 21:28:13 2017 +0200

    Delayed rollforward of commit 8fb311b4dced234b2f799c16c7d08148619f4087.

    This was rolled back due to Tensorflow breakage but the patch I exported to gerrit (https://bazel-review.googlesource.com/c/bazel/+/18590) passed Tensorflow (https://ci.bazel.io/job/bazel/job/presubmit/52/Downstream_projects/). Confirmed with jcater@ that the "newly failing" projects in the Global Tests are known issues. I think we can check this in now.

    Additionally I had attempted to reproduce any tensorflow issues with this by building and testing TensorFlow locally with this patch, and all tests which passed with the released bazel had also passed with this patch.

    ================= Original change description ==========================

    Reinstate idleness checks where the server self-terminates when it's idle and there is either too much memory pressure or the workspace directory is gone.

    Arguably, it should kill itself when the workspace directory is gone regardless of whether it's idle or not, but let's first get us back to a known good state, then we can think about improvements.

    RELNOTES: None.
    PiperOrigin-RevId: 172361085