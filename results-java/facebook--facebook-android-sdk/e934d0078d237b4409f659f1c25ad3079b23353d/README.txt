commit e934d0078d237b4409f659f1c25ad3079b23353d
Author: Chris Lang <clang@fb.com>
Date:   Wed Aug 15 18:02:18 2012 -0700

    [android-sdk] Improve performance of GraphObjectWrapper

    Summary:
    GraphObjectWrapper was using Method.equals to determine whether the invoked method was one it could handle. This
    suffers from poor performance, and for our purposes it suffices to test whether the method names are equal (none of
    the methods we implement are overloaded). This change improves performance of the FriendPickerView (in progress) by
    approximately 300% when sorting 750 friends.

    Test Plan:
    - Ran unit tests

    Revert Plan:

    Reviewers: mmarucheck, jacl, gregschechte, ayden

    Reviewed By: mmarucheck

    CC: msdkexp@, platform-diffs@lists

    Differential Revision: https://phabricator.fb.com/D550038