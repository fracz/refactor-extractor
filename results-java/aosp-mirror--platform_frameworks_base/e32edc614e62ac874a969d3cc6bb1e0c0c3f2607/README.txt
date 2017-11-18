commit e32edc614e62ac874a969d3cc6bb1e0c0c3f2607
Author: Romain Guy <romainguy@android.com>
Date:   Fri May 29 10:33:36 2009 -0700

    Fixes #1884152. This change improves how the opaque property is handled with respect to dividers.

    If the list is opaque and its background is not, then we want to fill a solid rect where the dividers should be when they are skipped for non-selectable items. When the list is opaque and the background is also opaque, there is no need to draw something in lieu of the dividers since the background will do it for us.