commit 8a439ac7a34d6b83782a672f3d6aa90fa262409c
Author: Gilles Debunne <debunne@google.com>
Date:   Wed Oct 26 16:41:28 2011 -0700

    Performance improvement in TextView

    Using a SpanSet to minimize the number the calls to getSpans.

    This is a cherry pick of 145653 in ICS-MR1

    Change-Id: I0a6e1fc7bd7a89325c2925bf98d59626d5e12995