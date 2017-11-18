commit f5d1e35316fcd027e60655b7ca6ca6341ed1e0ec
Author: Wale Ogunwale <ogunwale@google.com>
Date:   Thu Sep 22 08:34:42 2016 -0700

    Don't take app screenshot in minimize docked stack.

    When the docked stack is minimized its app windows are cropped
    significantly so any screenshot taken will not display the apps
    contain. So, we avoid taking a screenshot in that case.

    This situation is going to be improve some in O when we switch
    recents to also use saved surfaces in which cases it will be able
    to display the last app content the user saw vs. just the app's
    background color.

    Bug: 29830173
    Change-Id: I003aa074126ddcb57d5ff6dda756293146646196