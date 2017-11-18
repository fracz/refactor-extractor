commit 9535c915b61c43d7b4bdb2b35d53774cfb16bfbb
Author: Amith Yamasani <yamasani@google.com>
Date:   Wed Oct 10 21:48:33 2012 -0700

    Fix a runtime restart when cycling between 4 users

    Some refactoring in Sync Manager caused mUserManager to be initialized too late.
    Make sure this is initialized in the constructor now.

    Bug: 7328386
    Change-Id: Ic67915e172c3b8ef368852147667287e38c0213b