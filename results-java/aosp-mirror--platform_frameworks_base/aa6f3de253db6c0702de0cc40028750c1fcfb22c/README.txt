commit aa6f3de253db6c0702de0cc40028750c1fcfb22c
Author: Svetoslav Ganov <svetoslavganov@google.com>
Date:   Mon Apr 9 17:39:00 2012 -0700

    Some view not shown on the screen are reported for accessibility.

    1. Some applications are keeping around visible views off screen
       to improve responsiveness by drawing them in layers, etc. While
       such a view is not visible on the screen the accessibility layer
       was reporting it since it was visible. Now the check is improved
       to verify whether the view is attached, is in visible window,
       is visible, and has a rectangle that is not clipped by its
       predecessors.

    2. AccessibilityNodeInfo bounds in screen were not properly set
       since only the top left point was offset appropriately to
       take into account any predecessor's transformation matrix
       and the not transformed width and height were used. Now
       the bounds are properly offset.

    bug:6291855

    Change-Id: I244d1d9af81391676c1c9e0fe86cf4574ff37225