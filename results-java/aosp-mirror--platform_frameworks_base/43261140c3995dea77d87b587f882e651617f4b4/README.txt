commit 43261140c3995dea77d87b587f882e651617f4b4
Author: Benjamin Franz <bfranz@google.com>
Date:   Wed Feb 11 15:59:44 2015 +0000

    Clean up the lock task APIs for COSU devices.

    Clean up and increase readability of internal handling of lock task mode APIs.
    Add a public API to query the lock task mode state with pinned and locked as
    possible outcomes. Additionally, change wording in lock task toasts when in
    locked mode and update the javadoc regarding onLockTaskModeEntering and
    onLockTaskModeExiting to represent the actual behaviour.

    Bug: 19377096
    Change-Id: Ia563078ca6ef6d6fc7e75130e6b94ba18af69340