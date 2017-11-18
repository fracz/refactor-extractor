commit 258341c377b6aa9f1bd29a9b507a97967e432dfe
Author: Jim Miller <jaggies@google.com>
Date:   Thu Aug 30 16:50:10 2012 -0700

    Lots of keyguard improvements
    - Fix "too many attempts" dialogs
    - Fix account unlock mechanism so the user can use email account as backup for pattern unlock
    - Add mechanism to support future account recovery from non-pattern screen
    - Tune animation timing for flipping security view.
    - Move password field to the top of the security view
    - Add padding and visual feedback to navigation area button

    Fixes bugs 7088482, 7088631

    Change-Id: I23099feae3b7446ec291d8f860601bfc12f9edd8