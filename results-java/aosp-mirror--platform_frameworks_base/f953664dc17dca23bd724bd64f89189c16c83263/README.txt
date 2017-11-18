commit f953664dc17dca23bd724bd64f89189c16c83263
Author: Chris Wren <cwren@android.com>
Date:   Thu Apr 17 10:01:54 2014 -0400

    notification ranking infrastructure

    Added an ordered list of notifications (n.b. a complete ordering).
    Added a mechanism for ranking to be updated asynchronously
    Added onNotificationRankingUpdate to NotificationListeners
    Added an opaque order update object and a convenience comparator that
      uses it to sort notifications for listeners

    Repurpose scorers to be ranking preprocessors.  The preprocessors will
    perform heavy-weight validation of the notification object and memoize
    the results to improve efficiency of the ranking comparator.

    Current internal comparator implements status quo ordering, except
    that notes with a valid contact sort to the top of their priority
    bucket.

    Change-Id: I7244c65944a9657df41fb313b3cb5a52e149709d