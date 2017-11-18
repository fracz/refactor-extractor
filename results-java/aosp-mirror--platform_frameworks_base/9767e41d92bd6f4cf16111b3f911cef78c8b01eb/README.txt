commit 9767e41d92bd6f4cf16111b3f911cef78c8b01eb
Author: Dianne Hackborn <hackbod@google.com>
Date:   Tue Sep 15 18:45:34 2009 -0700

    Some improvements for wallpaper configuration.

    This introduces a new activity that you can derive from to implement
    a wall paper configuration activity.  This is supposed to select
    a theme based on whether it is being run to configure a real wallpaper
    or a preview, but this is going to be more difficult to do than I
    thought. :(

    Also fix a problem in the white theme where the list view's background
    was being set to white, so it wouldn't work on a transparent bg.

    Change-Id: I26d5a8695a3c878a1664eb09900eded57eaff990