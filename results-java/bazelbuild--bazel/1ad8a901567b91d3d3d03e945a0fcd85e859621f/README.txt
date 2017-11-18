commit 1ad8a901567b91d3d3d03e945a0fcd85e859621f
Author: Liam Miller-Cushon <cushon@google.com>
Date:   Thu Jan 5 23:56:06 2017 +0000

    Use the new turbine implementation with Bazel

    This improves performance with --java_header_compilation enabled compared to
    javac-turbine, and lays groundwork for some future optimizations.

    --
    PiperOrigin-RevId: 143719507
    MOS_MIGRATED_REVID=143719507