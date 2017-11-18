commit 7498efdc5e163d6b4a11db941c7d13c169d37284
Author: Svet Ganov <svetoslavganov@google.com>
Date:   Wed Sep 3 21:33:00 2014 -0700

    Clicking on partially coverd views by other views or windows.

    In touch exploration mode an accessibility service can move
    accessibility focus in response to user gestures. In this case
    when the user double-taps the system is sending down and up
    events at the center of the acessibility focused view. This
    works fine until the clicked view's center is covered by another
    clickable view. In such a scenario the user thinks he is clicking
    on one view but the click is handled by another. Terrible.

    This change solves the problem of clicking on the wrong view
    and also solves the problem of clicking on the wrong window.
    The key idea is that when the system detects a double tap or
    a double tap and hold it asks the accessibility focused node
    (if such) to compute a point at which a click can be performed.
    In respinse to that the node is asking the source view to
    compute this.

    If a view is partially covered by siblings or siblings of
    predecessors that are clickable, the click point will be
    properly computed to ensure the click occurs on the desired
    view. The click point is also bounded in the interactive
    part of the host window.

    The current approach has rare edge cases that may produce false
    positives or false negatives. For example, a portion of the
    view may be covered by an interactive descendant of a
    predecessor, which we do not compute (we check only siblings of
    predecessors). Also a view may be handling raw touch events
    instead of registering click listeners, which we cannot compute.
    Despite these limitations this approach will work most of the
    time and it is a huge improvement over just blindly sending
    the down and up events in the center of the view.

    Note that the additional computational complexity is incurred
    only when the user wants to click on the accessibility focused
    view which is very a rare event and this is a good tradeoff.

    bug:15696993

    Change-Id: I85927a77d6c24f7550b0d5f9f762722a8230830f