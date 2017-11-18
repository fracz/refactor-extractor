commit aac13242c1e2bb6d1870e1284795bd3ca370984c
Author: nharmata <nharmata@google.com>
Date:   Mon Apr 24 17:41:23 2017 +0200

    Make PathFragment an abstract class.

    There are now four concrete implementations: RelativeUnixPathFragment, AbsoluteUnixPathFragment, RelativeWindowsPathFragment, AbsoluteWindowsPathFragment.

    Goals:
    -Reduce memory usage of PathFragment on non-Windows platforms while maintaining existing semantics.
    -Make a few simple performance improvements along the way.
    -Add TODOs for a few more simple performance improvements.
    -Open the way for reducing code complexity of PathFragment. All of the Windows-specific stuff ought not complicate the code so much.

    Non goals:
    -Make the entire codebase as pretty as possible wrt PathFragment & Windows.
    -Make PathFragment usage more sane in general (e.g. change semantics to ban coexistence of Windows and Unix PathFragments).
    -Optimize PathFragment as much as possible wrt memory or even in any other dimensions (e.g. gc churn, cpu).

    To elaborate, the primary motivation is per-instance memory usage of PathFragment on Unix platforms:

    Before this change
    ------------------
    +UseCompressedOops --> 32 bytes per instance
    -UseCompressedOops --> 40 bytes per instance

    After this change
    ------------------
    +UseCompressedOops --> 24 bytes per instance
    -UseCompressedOops --> 32 bytes per instance

    Since Bazel can retain lots of PathFragments, the memory savings of this CL are fairly large.

    RELNOTES: None
    PiperOrigin-RevId: 154052905