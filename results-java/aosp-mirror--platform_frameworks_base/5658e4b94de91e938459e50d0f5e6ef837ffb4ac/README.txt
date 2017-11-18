commit 5658e4b94de91e938459e50d0f5e6ef837ffb4ac
Author: Wale Ogunwale <ogunwale@google.com>
Date:   Fri Feb 12 12:22:19 2016 -0800

    Don't resume activity on start if there are activities pausing.

    The path to start an activity if it isn't running was unconditionally
    resuming the starting activity which we don't do if there are activities
    pausing. It now starts the activity in a paused state if other activities
    are pausing. It is then resumed when pause completes.

    Also, improved logging in BoundsAnimationController and removed some
    disabled code that has been in the codebase for 6yrs...

    Bug: 26982752
    Change-Id: Ie042fc938331127f1270fca1b5905b067b9dae7c