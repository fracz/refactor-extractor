commit 2610b7cb2a197a3f3f8f7c3409b6962353684159
Author: Dianne Hackborn <hackbod@google.com>
Date:   Fri Sep 20 18:45:43 2013 -0700

    Fix issue #10863270: procstats UI is showing all green

    Gah I messed up when refactoring so it would always be told
    RAM is low.

    Also slightly tune the low memory parameters to go into low
    memory states a bit more aggressively.

    Change-Id: I5f970349760ad349d515a85c266ab21b387ee353