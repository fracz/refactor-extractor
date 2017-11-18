commit d0132e8e187ebf69bf4d2d6d0ef0027ff3f7a727
Author: Jeff Brown <jeffbrown@google.com>
Date:   Wed Apr 6 15:33:01 2011 -0700

    Minor Alt-TAB / Recent Apps Dialog improvements. (DO NOT MERGE)

    Alt-TAB should have different semantics from the APP_SWITCH key
    or long-press on HOME.  Accordingly, remove the fallback action
    for Alt-TAB and initiate the task switching behavior directly
    in the policy.

    Modified RecentApplicationsDialog to be more precise about the
    initial modifiers that it considers to be holding the dialog.

    The dialog is now dismissed by a second press on the APP_SWITCH
    key or by a second long press on HOME.

    Change-Id: I07e72dc4e1f3cd8edaf357c1d49e79f60d6d1604