commit ddca3ee3e86fbaa05c1528bd72afd955f0fb4ee6
Author: Dianne Hackborn <hackbod@google.com>
Date:   Thu Jul 23 19:01:31 2009 -0700

    Add support for power keys, improve behavior of virtual keys.

    The platform now knows how to deal with a platform key, which at this
    point is "just like end call, but don't end a call."

    Also improve the handling of virtual keys, to allow for canceling when
    sliding off into the display and providing haptic feedback.

    Finally fixes a bug where the raw x and y in motion event were not
    always set which caused the status bar to not work.