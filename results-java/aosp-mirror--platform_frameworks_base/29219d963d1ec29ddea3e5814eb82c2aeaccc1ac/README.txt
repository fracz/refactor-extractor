commit 29219d963d1ec29ddea3e5814eb82c2aeaccc1ac
Author: Craig Mautner <cmautner@google.com>
Date:   Tue Apr 16 20:19:12 2013 -0700

    Steady improvement to multi stack.

    - Fix back button behavior with two stacks. Stopping activities were
    held in that state indefinitely. This change causes IDLE_NOW_MSG to
    be sent immediately for the last activity in a stack.

    - Touch in non-focused stack was being ignored because of focus tests
    in AbsListView.

    - Change the focused stack when the activity focus changes. Renamed
    mMainStack to mFocusedStack to reflect this.

    - Resume all top stack activities when resuming.

    - Assign intent task to ActivityRecord if it doesn't have a task.
    Fixes bug 8433463.

    Change-Id: I8d3c806234511697bc209ab99890730ffa514d20