commit ad9ef191f50767d8d5b6f0fbd4b59bb1400dcd25
Author: Jeff Brown <jeffbrown@google.com>
Date:   Tue Apr 8 17:26:30 2014 -0700

    Move display power controller to display manager service. (DO NOT MERGE)

    This refactoring is in preparation for enabling the display manager
    to have more control over the blanking state of individual displays.
    There are no functional changes.  Some bits will be cleaned up in
    a subsequent patch.

    Bug: 13133142
    Change-Id: Ib811835e8757449c7899ac61807029baaf998161