commit de3321d13bcac4108affa2a80de9c7d30eaa0ce6
Author: Sid Soundararajan <ssoundar@google.com>
Date:   Wed May 11 14:56:21 2016 -0700

    Move onStart update of recents tasks to onResume

    This is needed to ensure that the update happens between app
    launches, since translucent apps do not get to onStop(). Part of fixes
    to improve performance for bug.

    BUG: 28371792
    Change-Id: I47e3a20bba4f006bfc637635d9c9af697a7fc648