commit 6579b0b4ac0e781efab044aaaf3f66447cf5e067
Author: Adam Powell <adamp@google.com>
Date:   Thu Mar 25 12:21:34 2010 -0700

    API refactoring for OverScroller.

    OverScroller is no longer a child class of Scroller and several
    Scroller methods that do not make sense for OverScroller and could
    cause misbehaving edge cases have been removed or hidden and
    deprecated.

    Change-Id: Ie055b607bd3b36c47ab9798d5c9518aef686b474