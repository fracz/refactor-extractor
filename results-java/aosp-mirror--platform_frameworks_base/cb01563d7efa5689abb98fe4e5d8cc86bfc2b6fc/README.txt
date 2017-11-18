commit cb01563d7efa5689abb98fe4e5d8cc86bfc2b6fc
Author: Dianne Hackborn <hackbod@google.com>
Date:   Wed Jun 14 17:30:15 2017 -0700

    Work on issue #36891897: Need to ensure foreground services...

    ...can't hide themselves

    Tune the policies for when we tell about apps running in the
    background after their services have stopped.

    - If it ran while the screen was on, the time we require for it
    to be running is much shorter (a couple seconds) as well as the
    time we tell about it having run (with another tunable for the
    minimum time we tell about this).

    - If it has only run while the screen is off and stops a sufficient
    amount of time before the screen goes on (currently a second) then
    we will not show anything when the screen goes on.

    - If it stops when the screen turns on, we will make sure the user
    sees about it for a short period of time (currently 5 seconds).

    Also includes some improved debug output about handler message
    queues.

    Test: manual

    Change-Id: Iab438410d7182b2dfe4f9c1cce7069b26b34834c