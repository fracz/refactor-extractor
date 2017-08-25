commit 2c8eff9b530098fc9690b92dcab2fd46cdd1f91c
Author: Jonathan Vollebregt <jnvsor@gmail.com>
Date:   Tue May 31 21:02:16 2016 +0200

    Update AdminerTablesFilter

    * Removes children request (Should work on IE6, 7, 8 now)
    * Uses <strong> instead of <b>
    * Doesn't leave said <strong> tags behind after updating the list
    * Highlights multiple matches in a single table name
    * Works case insensitively
    * Improves performance with setTimeout
        With 400 tables, the old implementation locks up the tab (or
        browser if using something without multiprocess) for about
        half a second per keyup. Yes, this includes modifiers that
        don't actually change the filter. The new version handles
        the same event in 0.09 milliseconds.

        That's with all the above improvements.
        Tested in firefox 45.1.1 performance monitor.