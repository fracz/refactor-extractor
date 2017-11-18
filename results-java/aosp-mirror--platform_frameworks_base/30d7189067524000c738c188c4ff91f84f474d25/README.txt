commit 30d7189067524000c738c188c4ff91f84f474d25
Author: Dianne Hackborn <hackbod@google.com>
Date:   Sat Dec 11 10:37:55 2010 -0800

    Fix issue #3274841: Orientation change problem with a paused activity

    Plus a bunch of debug output improvements.

    And some new Intent helpers for dealing with restarting an app.

    Change-Id: I50ec56bca6a86c562156b13fe8a6fdf68038a12e