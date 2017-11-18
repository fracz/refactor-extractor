commit 354137f5af34c603334458bbabed4fca91b1d2ed
Author: ajmichael <ajmichael@google.com>
Date:   Thu Mar 30 21:21:41 2017 +0000

    Build android_binary APKs with Singlejar by default.

    This will improve android_binary build times and allow Bazel to remove a
    dependency on our forked version of the deprecated ApkBuilderMain.

    RELNOTES: None

    PiperOrigin-RevId: 151749709