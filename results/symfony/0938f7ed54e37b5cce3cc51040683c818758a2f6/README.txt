commit 0938f7ed54e37b5cce3cc51040683c818758a2f6
Merge: 4824926 2d29a82
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Thu Jun 23 11:33:28 2011 +0200

    merged branch lenar/non-blocking-process (PR #1403)

    Commits
    -------

    2d29a82 New test for Process, testing stdout and stderr at different stream sizes

    Discussion
    ----------

    Make run() fully non-blocking and fix potential other problems

    Multiple changes:

    1) make writing to process non-blocking too - otherwise there might be increased possibility for buffer deadlock
    given big enough input data. Also now it's guaranteed that all stdin data will be written.

    2) get rid of fgets() - fgets() isn't really good function to use in case of non-blocking sockets. Data loss possible.

    ---------------------------------------------------------------------------

    by fabpot at 2011/06/22 07:11:55 -0700

    Does it make https://github.com/symfony/symfony/pull/1365 obsolete?

    ---------------------------------------------------------------------------

    by lenar at 2011/06/22 14:08:14 -0700

    @fabpot: After reading, I really don't know. Let's hope. But ...

    I now improved Process tests a bit to test stdout, stderr with different stream sizes and different
    behaviours of child processes. Added it to non-blocking-process branch, commit 2d29a82412702faf5137.
    In my case, nothing fails, but maybe this helps other people. Or Windows people - I myself cannot test on Windows.

    ---------------------------------------------------------------------------

    by fabpot at 2011/06/22 22:59:55 -0700

    These tests pass on my Linux box but fail on my Mac.

    ---------------------------------------------------------------------------

    by fabpot at 2011/06/22 23:05:14 -0700

    Actually, on the Mac, the tests behave correctly but the exit code is `-1` instead of `0`.

    ---------------------------------------------------------------------------

    by lenar at 2011/06/23 01:23:51 -0700

    Could you check if the $this->status['running'] (after call to proc_get_status()) is true in the case you get -1.

    On my linux I got it -1 couple of times. 99% of time it doesn't happen. I theorized it's because sometimes the child
    process isn't finished enough and finally I got confirmation too that in case of -1 the process is still running (stats['running'] === true).

    But it's really almost unreproducible on my Linux. So if you have this value every time it might be easier for you to find solution.

    What comes into my mind:

    1) maybe we should poll, let's say if process is still running we usleep(1000) and the try proc_get_status() again until not running. Maybe up to a 1 sec.

    2) maybe, if the process is still running we can trust the return value subsequently given by proc_close()?

    Or maybe there's some other problem on Mac.