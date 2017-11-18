commit c24473bf0aa4c36632eb2051d0bd4f53fb973dba
Author: Jason Sendros-Keshka <jsendros@fb.com>
Date:   Fri Jul 14 13:37:33 2017 -0700

    Add requestFocusWithOffset API

    Summary:
    RecyclerView's scrollToPosition API will silently do nothing if that position is visible anywhere on the screen. In the case where you want to scroll that item to the top of the screen even if it's already on the screen somewhere else, you need to use scrollToPositionWithOffset. This adds the API necessary to enforce that. Also, if you want to scroll to a position with an offset, you can do that, too.

    Why I didn't refactor requestFocus() to delegate to this with offset 0:
    1) I didn't want to break existing callers. There aren't many of them yet, so if this is the only problem, I can reach out and see if they're OK with the change.
    2) People familiar with this quirk in the RecyclerView library might do the wrong thing. On the other hand, I think the default behavior of scrolling that item to the top makes more sense with the exception to the rule being to do nothing if it's on screen anywhere. I'd be down to refactor `requestFocus` to delegate to this with an override to do the other thing as the edge case.

    Reviewed By: mihaelao

    Differential Revision: D5423905

    fbshipit-source-id: 07cc54fa074f1566884016fffb1ea07e9cc6ffed