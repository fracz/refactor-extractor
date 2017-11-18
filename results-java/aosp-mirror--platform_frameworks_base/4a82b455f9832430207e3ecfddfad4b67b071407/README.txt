commit 4a82b455f9832430207e3ecfddfad4b67b071407
Author: Svetoslav Ganov <svetoslavganov@google.com>
Date:   Sat Oct 12 19:04:53 2013 -0700

    Printing from secondary user not working.

    The last refactoring of how the print dialog activity is started moved
    the code that creates the pending intent from the spooler which is a
    per user app to the system process but failed to create the intent
    for the right user. Also the code in the print manager service that
    puts a notification for a newly isntalled print service was not taking
    into account the current user.

    bug:11199393

    Change-Id: I64ecf9dc1457ec4d58cc1a62e53735bb0793a003