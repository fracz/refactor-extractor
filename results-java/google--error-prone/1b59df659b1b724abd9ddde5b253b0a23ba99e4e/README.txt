commit 1b59df659b1b724abd9ddde5b253b0a23ba99e4e
Author: cushon <cushon@google.com>
Date:   Tue May 31 18:20:13 2016 -0700

    Delete FallThroughSuppression

    It was a one-time refactoring to remove
    @SuppressWarnings("fallthrough"); I don't think it has any value as an
    actual check.

    MOE_MIGRATED_REVID=123706099