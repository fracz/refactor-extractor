commit 4496992ccdf9f05573c912cfe181b143cff63fce
Author: xtof <xtof@google.com>
Date:   Tue Mar 22 11:45:24 2016 -0700

    RefasterJS: Provide a flag to allow changing the type matching strategy to be used
    when matching JSTypes in templates.

    Also, rename TypeMatchingStrategy#DEFAULT to LOOSE to make it clearer what
    it does. In some contexts, loose type matching is undesirable and not such a
    great default (notably RefasterJS, where it can lead to refactored code that
    doesn't type-check).
    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=117846339