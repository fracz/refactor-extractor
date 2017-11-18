commit 693d869b32b375a6b3812aa56145e334b3a8c219
Author: Amith Yamasani <yamasani@google.com>
Date:   Tue Sep 13 14:24:01 2016 -0700

    Handle charging state separately from temporary parole

    Don't use the parole state to deal with the plugged-in trigger.
    Otherwise standby apps will only be paroled for a few minutes
    after plugging in to charge and not the entire duration.

    Use a different intent for charging state, since the CHARGING
    intent takes several seconds to be received.

    A refactor some time ago changed the charging state behavior
    that caused this regression.

    Bug: 31399882
    Change-Id: Ic036de5e136b3151b225473d0c3f440adb3b48e7