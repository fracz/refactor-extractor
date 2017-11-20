commit 8eabc0d8a410eadaf2f97f23ba764de05350adfa
Author: Ben Manes <ben.manes@gmail.com>
Date:   Sun Aug 30 01:08:42 2015 -0700

    Finish simulator's LIRS implementation

    This version now exceeds the hit rate compared to the reference
    implementations, which diverge from the papers often by mistake. This
    version is the closest to the C version provided by the authors. There
    appears to be a slight difference which I believe is a bug in their
    code (per their papers), but the difference is negligable.

    There remains two pending tasks left to resolve. The maximum number of
    non-resident entries must be bounded and the papers do not discuss this
    requirement. The author's C version does this by restricting the length
    of S. The Jackrabbit version uses a separate non-resident queue that is
    capped. An approach should be chosen and the size configurable.

    The Jackrabbit version includes an optimization where the hottest
    LIR entries are not reordered on a read. This improves the concurrency
    at, hypothetically, minimal impact to the hit rate. This technique
    should be implemented, the percentage configurable, and experimented
    with to find the optimal value.