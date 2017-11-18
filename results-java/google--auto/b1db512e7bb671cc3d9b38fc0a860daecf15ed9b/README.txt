commit b1db512e7bb671cc3d9b38fc0a860daecf15ed9b
Author: cgdecker <cgdecker@google.com>
Date:   Mon Mar 7 08:33:50 2016 -0800

    Rely on Guava 19.0 and use CharMatcher.whitespace() since CharMatcher.WHITESPACE is now soft-deprecated and will be removed in a future version of Guava (improves performance on android - not relevant to auto-value, but still nicer).

    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=116549017