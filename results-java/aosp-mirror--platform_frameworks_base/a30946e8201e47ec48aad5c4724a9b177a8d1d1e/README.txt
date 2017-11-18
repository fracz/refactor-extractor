commit a30946e8201e47ec48aad5c4724a9b177a8d1d1e
Author: Gilles Debunne <debunne@google.com>
Date:   Wed Oct 26 16:41:28 2011 -0700

    Performance improvement in TextView

    Using a SpanSet to minimize the number the calls to getSpans.

    Change-Id: I0a6e1fc7bd7a89325c2925bf98d59626d5e12995