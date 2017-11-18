commit 165ef0d34adffb8a5ac84b859e9d58cd68412193
Author: jcarnahan <jcarnahan@google.com>
Date:   Tue Jun 28 07:51:34 2016 -0700

    Mention getOnlyElement() as an alternative in the Javadoc for getFirst().

    This came up in my Java readability review ([]as I had been
    aware of getFirst() but not of getOnlyElement(). Indeed, I think many of the
    cases where it's valid to only look at one element from an iterator are
    actually cases where there's only one element in the iterator, and so I suspect
    I won't be the only person who's using getFirst() when they are really looking
    for getOnlyElement() instead.

    This CL also reformats the rest of the Javadoc for getFirst() to 100 characters
    to be consistent with the new paragraph that I added.
    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=126073627