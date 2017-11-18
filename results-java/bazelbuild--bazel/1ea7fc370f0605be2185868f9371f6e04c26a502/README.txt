commit 1ea7fc370f0605be2185868f9371f6e04c26a502
Author: John Field <jfield@google.com>
Date:   Tue Dec 22 19:37:19 2015 +0000

    Enable label-based Skylark loading. In particular, such labels may reference files in external repositories.

    In addition:

    - Cleaned up and refactored some tests to reflect the new loading behavior.

    Deferred to future CLs:

    - Updating Bazel Skylark documentation to reflect the new load form.

    - Enabling command-line loading of Aspects via labels.

    RELNOTES: Skylark load statements may now reference .bzl files via build labels, in addition to paths. In particular, such labels can be used to reference Skylark files in external repositories; e.g., load("@my_external_repo//some_pkg:some_file.bzl", ...). Path-based loads are now deprecated and may be disabled in the future. Caveats: Skylark files currently do not respect package visibility; i.e., all Skylark files are effectively public. Also, loads may not reference the special //external package.

    --
    MOS_MIGRATED_REVID=110786452