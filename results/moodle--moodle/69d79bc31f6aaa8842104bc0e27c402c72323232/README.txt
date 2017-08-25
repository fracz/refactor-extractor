commit 69d79bc31f6aaa8842104bc0e27c402c72323232
Author: moodler <moodler>
Date:   Sat Jan 31 14:47:57 2004 +0000

    OK, some big improvements to the logs.

    Logs now include a field called modid which contains the coursemodule id.

    This makes it now possible to

       - see complete logs per-activity

       - do backup/restore of logs

    The upgrade process will currently try to scan all the old logs and
    rebuild this field based on available data (especially forums).

    STILL TO DO:  alter all the non-forum modules to send the coursemodule id