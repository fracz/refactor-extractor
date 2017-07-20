commit 6c0db645eafb257eff599d8c85f99537df34d379
Author: Thomas Steur <thomas.steur@gmail.com>
Date:   Sun Feb 8 22:54:19 2015 +0000

    refs #6809 a couple of performance improvements for the all websites report.

    I tested it with 4k websites. The change made it only a couple of seconds faster
    but requesting 4k websites would still take 4-8 seconds even if only 10 sites of
    them are actually requested. For example before we did request each site 3 times
    (12k times in total and triggered 12k events etc) which should be no longer the case.
    The last `clearSiteCache` was there to free some memory but I think it shouldn't
    be problematic to remove it. Also I removed one 'sort' as a lot of time is wasted
    there. It should be in theory sorted afterwards anyway again. Need to see test
    results whether this change is good or not.