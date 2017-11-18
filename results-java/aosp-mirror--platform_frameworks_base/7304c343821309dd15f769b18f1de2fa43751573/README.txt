commit 7304c343821309dd15f769b18f1de2fa43751573
Author: Jeff Brown <jeffbrown@google.com>
Date:   Fri May 11 18:42:42 2012 -0700

    Move power HAL interactions to PowerManagerService.

    This refactoring sets the stage for a follow-on change that
    will make use additional functions of the power HAL.

    Moved functionality from android.os.Power into PowerManagerService.
    None of these functions make sense being called outside of the
    system server.  Moving them to the PowerManagerService makes it
    easier to ensure that the power HAL is initialized exactly once.

    Similarly, moved ShutdownThread out of the policy package and into
    the services package where it can tie into the PowerManagerService
    as needed.

    Bug: 6435382
    Change-Id: I958241bb124fb4410d96f5d5eb00ed68d60b29e5