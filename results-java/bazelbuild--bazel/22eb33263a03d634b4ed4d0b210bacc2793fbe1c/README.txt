commit 22eb33263a03d634b4ed4d0b210bacc2793fbe1c
Author: Yun Peng <pcloudy@google.com>
Date:   Tue Jun 21 14:38:12 2016 +0000

    Fixed two Bazel java tests on Windows by using the right native file system

    Newly passing:
    //src/test/java/com/google/devtools/build/...
      lib/skyframe:SkyframeTests
      lib:actions_test

    Also refactored FileSystems.java

    --
    Change-Id: I03ab9db5c1ab5e5be4ff1efbc5cf2d280084254a
    Reviewed-on: https://bazel-review.googlesource.com/#/c/3843
    MOS_MIGRATED_REVID=125449456