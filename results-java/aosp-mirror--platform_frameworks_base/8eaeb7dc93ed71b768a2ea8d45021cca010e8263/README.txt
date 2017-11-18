commit 8eaeb7dc93ed71b768a2ea8d45021cca010e8263
Author: Winson Chung <winsonc@google.com>
Date:   Wed Jun 25 15:10:59 2014 -0700

    Fixing bug where search bar is not updated when search package is first installed.

    - Fixing issue where we weren't disabling HW layers when you don't finish a swipe-to-dismiss
    - Preventing tapping on a task that is currently being dismissed
    - Adding a debug trigger for internal testing
    - Minor refactoring

    Change-Id: Id7dcc8a4b5a080402c2761cd555b8a882498ad29