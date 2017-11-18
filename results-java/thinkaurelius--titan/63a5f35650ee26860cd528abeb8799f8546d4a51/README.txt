commit 63a5f35650ee26860cd528abeb8799f8546d4a51
Author: Dan LaRocque <dalaro@hopcount.org>
Date:   Fri Jun 20 05:26:41 2014 -0400

    Refactor EVCS lock-checking logic

    This change is mostly a refactoring and javadoc commit in the
    ExpectedValueChecking* classes.

    However, it also includes a functional change.  Previous to this
    commit, locks taken on stores without any mutations (stores treated as
    read-only) would be ignored.  Now locks are checked regardless of
    whether the underlying store has any data mutations waiting.