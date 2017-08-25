commit 4c501ba70d29ef26332919877dcc9dd9a674c049
Author: Stefan Burnicki <stefan.burnicki@iteratec.de>
Date:   Tue Jun 21 15:42:20 2016 +0200

    refactor: Provide a variant of GetDevToolsRequests with only paths

    Can be used later on to load data for a step, not only a run.
    Caching was modified (`GetCachedDevToolsRequests`
    and `SaveCachedDevToolsRequests`). However it looks like
    it never was really in use sind `$ok = false;` was set
    after getting the cache.

    Also, the `[$run][$cache]` structure was not really important,
    since the cache file name is already specific for a (cached) run.