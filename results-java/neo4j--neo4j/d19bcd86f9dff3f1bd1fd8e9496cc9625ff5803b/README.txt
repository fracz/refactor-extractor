commit d19bcd86f9dff3f1bd1fd8e9496cc9625ff5803b
Author: Mattias Persson <mattias@neotechnology.com>
Date:   Fri Aug 4 13:04:07 2017 +0200

    More efficient log rotation checking

    Merely checking need for log rotation shows up on the radar when profiling,
    especially if there are many concurrent committers. When appending there's
    a natural selection of one out of all that gets to force the log.
    This commit piggy-backs on that decision and lets that thread, and only
    that one out of all concurrent committers, to check log rotation.

    Also introduced double checked locking for checking need for log rotation
    due to a lot of unnecessary hogging of that monitor in most calls,
    since log rotation isn't needed very often

    These two minor changes improves transaction commit concurrency