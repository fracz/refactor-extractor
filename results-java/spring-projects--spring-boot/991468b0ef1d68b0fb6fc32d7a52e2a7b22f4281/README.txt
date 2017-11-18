commit 991468b0ef1d68b0fb6fc32d7a52e2a7b22f4281
Author: Vedran Pavic <vedran.pavic@gmail.com>
Date:   Tue Jul 5 11:29:35 2016 +0200

    Use SizeAndTimeBasedRollingPolicy file appender

    Update the logback file appender to use `SizeAndTimeBasedRollingPolicy`
    rather than `FixedWindowRollingPolicy`.

    Add two new properties to improve log file configuration capabilities:

     - `logging.file.max-history` to limit the number of archive log files
        to keep.
     - `logging.file.max-size` to limit the log file size.

    See gh-6352