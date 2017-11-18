commit b8a9f12802ca5d1738a098a1769a8ed7959f248b
Author: Dianne Hackborn <hackbod@google.com>
Date:   Wed Sep 23 11:27:06 2009 -0700

    Fix #2101821: Cut labels of menu items in "SnapTell" market app.

    Ummmm...  this turns out to be pretty bad.  NinePatchDrawable was not
    scaling its reported padding for compatibility mode, causing spacing
    to be off.  All over the place.  This change should improve things quite
    a bit (and magically makes nearly all of the menu flaws go away).

    Change-Id: I94a8310d95b908b6f087db97d9afaed654ca6de5