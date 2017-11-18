commit 5523c5f829ef03aae24e8970be883183acbcf986
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Thu Jan 19 15:26:53 2012 +0100

    Improved daemon logging...

    On the way to improve daemon feedback on process failure.

    -Added basic coverage for the daemon log to contain all log entries (e.g. output from before started to log to client via connection, from after that, and from the build itself).
    -Slightly changed the executor so that it's possible to override the daemon base dir from a single test.
    -Made sure log file contains logging from before we start relaying logs to client. It's a combination of Peter's fixes and some original implementation. This needs peer review.