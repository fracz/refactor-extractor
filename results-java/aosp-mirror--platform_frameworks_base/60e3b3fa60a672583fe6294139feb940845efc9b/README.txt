commit 60e3b3fa60a672583fe6294139feb940845efc9b
Author: Gilles Debunne <debunne@google.com>
Date:   Tue Mar 13 11:26:05 2012 -0700

    Use SpanSet to accelerate Layout.drawBackground

    The main performance improvement should come from the fact
    that we entirely skip the loop (which calls getLineStart, getLineTop
    and getLineDescent on each line) in the frequent case where there
    are no LineBackgroundSpans.

    Change-Id: Ie2d3168521e88d43f1a4236da2b3e8447606df1e