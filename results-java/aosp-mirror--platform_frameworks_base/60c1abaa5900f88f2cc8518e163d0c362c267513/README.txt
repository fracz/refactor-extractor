commit 60c1abaa5900f88f2cc8518e163d0c362c267513
Author: Winson Chung <winsonc@google.com>
Date:   Tue Mar 14 17:47:42 2017 -0700

    Fixing bad refactoring of orientation calculation.

    - The orientation needs to be calculated from the screen width/height,
      which are set below the current calculation of the orientation (whoops).
      This bug was introduced in ag/1905010 and causes extra configuration
      changes due to the configuration orientation being set correctly before
      it is corrected on the next change (at the end of the transition).

    Bug: 36099204
    Test: android.server.cts.ActivityManagerPinnedStackTests
    Change-Id: Ieca1f73fe61fc1dcea106535c019b52d126b8743