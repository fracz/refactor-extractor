commit 8051053bf1ef961a5d45d30b3197b57443495602
Author: tdeegan <tdeegan@google.com>
Date:   Wed Sep 7 15:37:20 2016 -0700

    Standardize unit tests in PureFunctionIndentifierTest
    - use LINE_JOINER for easier readability and better line numbers when we get errors
    - no more verbose "....\n" + ... (new lines are standard across all tests now)

    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=132488684