commit 131206b8a9d07400d7c98aea50cc45c38769448f
Author: Jeff Brown <jeffbrown@google.com>
Date:   Tue Apr 8 17:27:14 2014 -0700

    Move display power controller to display manager service.

    This refactoring is in preparation for enabling the display manager
    to have more control over the blanking state of individual displays.
    There are no functional changes.  Some bits will be cleaned up in
    a subsequent patch.

    Bug: 13133142
    Change-Id: I159a060088344d8e6fcdf9208a1f242960f7ab90