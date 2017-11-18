commit 4d035ddd28cd4c31506bae26d9877d5012c31495
Author: Brian Silverman <bsilver16384@gmail.com>
Date:   Wed Nov 18 14:51:08 2015 +0000

    Allow %package(@foo//bar)%/path for cxx builtin include dirs.

    This allows using compilers downloaded in *_repository rules without
    ugly hacks like symlinks to bazel-out/../../external.

    I manually tested all of the %prefix%s which this refactors the
    implementations of.

    --
    Change-Id: Ie9931dfbed646b8b5c9cd7fba5e6df5cf0baa1f2
    Reviewed-on: https://bazel-review.googlesource.com/#/c/2200
    MOS_MIGRATED_REVID=108139097