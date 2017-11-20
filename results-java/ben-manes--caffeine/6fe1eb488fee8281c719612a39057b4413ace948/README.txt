commit 6fe1eb488fee8281c719612a39057b4413ace948
Author: Ben Manes <ben.manes@gmail.com>
Date:   Wed Oct 14 02:36:17 2015 -0700

    Restore write performance

    When rewriting the put() for correctness with zero weights, there were
    major performance hits.

    1. The loss of a fast-path to mimic a read when the update did not
    require extra policy handling (e.g. replace with same weight). This
    improved performance to 33% - 50% of the prior version.

    2. The use of compute() to perform an mutation is slower than a get()
    and requires creating a lambda (heap, not stack allocated). These two
    reduce performance significantly, but cannot be worked around directly.
    Instead they are only required for correctness of weights, which is not
    the common case. The fix is then to use specialized put() methods
    depending on if a custom weigher is used.