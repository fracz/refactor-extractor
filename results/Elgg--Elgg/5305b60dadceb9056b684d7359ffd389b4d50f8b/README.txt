commit 5305b60dadceb9056b684d7359ffd389b4d50f8b
Author: Ismayil Khayredinov <ismayil.khayredinov@gmail.com>
Date:   Fri Mar 11 10:02:18 2016 +0100

    feature(cron): improved cron logging

    Cron output is now logged and displayed in admin statistics, so as to
    indicate cron completion.

    Cron hooks triggered by _elgg_cron_run() now receive correct return value
    for a given period
    rather than the global output value.

    Cron monitor now echoes cron period name and completion timestamp rather
    than 1.

    Fixes #9474