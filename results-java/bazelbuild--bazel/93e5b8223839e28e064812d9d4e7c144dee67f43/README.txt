commit 93e5b8223839e28e064812d9d4e7c144dee67f43
Author: Lukacs Berki <lberki@google.com>
Date:   Thu Jun 11 10:00:35 2015 +0000

    Various improvements to Python support in Bazel:

    - Make it possible to select the Python2/Python3 binaries to be used on the command line. Ideally, we'd also support hermetic Python runtimes, but no one wants them at the moment, so let's not do speculative work.
    - Make the Python stub script compatible with Python3.
    - Use the Python3 binary for py_binaries that are built for it.

    --
    MOS_MIGRATED_REVID=95722749