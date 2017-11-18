commit 5487500cf3d9f6d7703ce0704cb91837aa95d716
Author: Jeff Brown <jeffbrown@google.com>
Date:   Wed Apr 6 15:33:01 2011 -0700

    Minor Alt-TAB / Recent Apps Dialog improvements.

    Alt-TAB should have different semantics from the APP_SWITCH key
    or long-press on HOME.  Accordingly, remove the fallback action
    for Alt-TAB and initiate the task switching behavior directly
    in the policy.

    Modified RecentApplicationsDialog to be more precise about the
    initial modifiers that it considers to be holding the dialog.

    The dialog is now dismissed by a second press on the APP_SWITCH
    key or by a second long press on HOME.

    Change-Id: Idf4d803f51103819057cb655ff3b770b7729e4be