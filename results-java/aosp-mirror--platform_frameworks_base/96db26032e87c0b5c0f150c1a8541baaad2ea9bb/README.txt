commit 96db26032e87c0b5c0f150c1a8541baaad2ea9bb
Author: Svet Ganov <svetoslavganov@google.com>
Date:   Fri Feb 19 09:05:04 2016 -0800

    Don't hold a lock while reading shared preferences from disk.

    Shared prefrences loads thir content from disk on a separate
    thread to improve performance, however it holds the lock
    the whole time  while reading from disk which as a result blocks
    operations that don't rely on reading data from being performed
    intil load completes, e.g. reguistering a prefernces change
    listener does not depend on having the data loaded.

    bug:5254577

    Change-Id: I5ad67b285631c34d5aadac7138ba8bfaa728cf94