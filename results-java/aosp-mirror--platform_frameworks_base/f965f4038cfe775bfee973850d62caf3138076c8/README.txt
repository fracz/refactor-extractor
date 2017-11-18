commit f965f4038cfe775bfee973850d62caf3138076c8
Author: Dianne Hackborn <hackbod@google.com>
Date:   Thu May 4 23:27:23 2017 -0700

    Fix issue #38037532: Toasts cause apps to become foreground

    ForegroundToken is now ImportanceToken, and doesn't actually
    cause an app to become foreground (that is not needed for
    toasts anyway).

    Also improved output and logging -- new logs for changing
    in key uid stats, and when force stopping services after a
    uid goes idle.

    Test: manual

    Change-Id: I44dd391bb8d37857be1359f4b7021dc8d2cd0285