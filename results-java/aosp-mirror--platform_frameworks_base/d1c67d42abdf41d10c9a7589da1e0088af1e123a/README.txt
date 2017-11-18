commit d1c67d42abdf41d10c9a7589da1e0088af1e123a
Author: Romain Guy <romainguy@android.com>
Date:   Fri May 29 13:53:16 2009 -0700

    Fixes #1878499.

    Ignore touch up events that happen after a gesture was cancelled. This fix also improves performance by ignoring move events that are within the touch threshold.