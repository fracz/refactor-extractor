commit 848cfdfe9b232f4d357526aeb432df0e22f2917c
Author: Andy Street <andrews@fb.com>
Date:   Thu May 4 11:00:13 2017 -0700

    When generating new animations, clear old animations from layout state

    Summary: We probably don't want to actually store these on the LayoutState, but that's pending a larger refactor.

    Reviewed By: marco-cova

    Differential Revision: D5003069

    fbshipit-source-id: 1ad0085211b994f279cd6518b539f976eeb37b6a