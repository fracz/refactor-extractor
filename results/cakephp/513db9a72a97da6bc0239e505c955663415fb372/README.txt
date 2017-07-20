commit 513db9a72a97da6bc0239e505c955663415fb372
Author: Mark Story <mark@mark-story.com>
Date:   Thu Jan 21 14:14:26 2010 -0500

    Removing repeated checks, as they've been refactored into Cache::increment() and Cache::decrement().
    Adding skipIf()'s to ApcEngineTest in case apc_inc, and apc_dec are not available.