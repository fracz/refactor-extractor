commit 0de7dcd126175a1bef77f2039b39df1eed4b4164
Author: Andreas Huber <andih@google.com>
Date:   Mon Apr 20 14:22:31 2009 -0700

    Ensure ongoing progress updates after using the scroll ball to adjust position.

    We use a single outstanding message of type SHOW_PROGRESS to refresh the progress
    bar's current position as well as textual display of time and duration. This message
    could get lost if the scroll ball was used to adjust the current playback position as
    we entered state "mDragging" which would cause the subsequent SHOW_PROGRESS message
    to be a no-op and would also cause it to not be re-enqueued.
    The change refactors the seekbar logic a little and makes sure that while dragging
    there isn't a pending SHOW_PROGRESS message in the queue and once dragging is over,
    exactly one SHOW_PROGRESS message is reenqueued.

    related to bug 1721227