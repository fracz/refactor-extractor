commit 1bd4aafd1b15851da7929d279ff4dce78a9bd928
Author: nharmata <nharmata@google.com>
Date:   Tue Oct 31 11:23:04 2017 -0400

    Cosmetic refactor of some of the helper methods used by ParallelSkyQueryUtils.RBuildFilesVisitor. Also a minor tweak of the batch size for feeding results to the callback.

    Also correctly use the packageSemaphore in SkyQueryEnvironment's non-parallel implementation of rbuildfiles.

    RELNOTES: None
    PiperOrigin-RevId: 174039067