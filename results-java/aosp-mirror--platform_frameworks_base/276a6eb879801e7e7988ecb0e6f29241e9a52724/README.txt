commit 276a6eb879801e7e7988ecb0e6f29241e9a52724
Author: Craig Mautner <cmautner@google.com>
Date:   Tue Nov 4 15:32:57 2014 -0800

    When keyguard exits use same anim for all windows

    The entering animations were only applied to the incoming windows
    one time. If those windows weren't drawn yet then they never had
    an animation assigned.

    Furthermore if a starting window was drawn in time it would get the
    animation but its main window would not get it if it weren't drawn.
    Even if an animation were assigned later they wouldn't be synced
    with each other.

    This change creates a single animation which is shared by all
    incoming windows. As windows are drawn they can then animate with
    the starting window.

    (Also refactorings to eliminate redundant code and unnecessary
    variables.)

    Fixes bug 15991916.

    Change-Id: I844d102439b6eda8c912108431916e04b12f7298