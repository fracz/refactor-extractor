commit 8fc51380de681f15a0467c0c484897f6c2044fa2
Author: Ryan Ernst <ryan@iernst.net>
Date:   Wed May 4 17:29:23 2016 -0700

    Tests: improve logging for vagrant to emit entire output on failure

    This change makes the vagrant tasks extend LoggedExec, so that the
    entire vagrant output can be dumped on failure (and completely logged
    when using --info). It should help for debugging issues like #18122.