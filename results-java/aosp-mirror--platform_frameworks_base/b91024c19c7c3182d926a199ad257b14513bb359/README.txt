commit b91024c19c7c3182d926a199ad257b14513bb359
Author: Dianne Hackborn <hackbod@google.com>
Date:   Wed Sep 23 11:27:06 2009 -0700

    Fix #2101821: Cut labels of menu items in "SnapTell" market app.

    Ummmm...  this turns out to be pretty bad.  NinePatchDrawable was not
    scaling its reported padding for compatibility mode, causing spacing
    to be off.  All over the place.  This change should improve things quite
    a bit (and magically makes nearly all of the menu flaws go away).