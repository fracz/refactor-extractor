commit 6447ca30b8e41c22c7214f201120327057e356dc
Author: Dianne Hackborn <hackbod@google.com>
Date:   Tue Apr 7 19:50:08 2009 -0700

    Fix issue #1769910 (Track activity launch times)

    The usage stats service now collects per-activity launch time stats.  There are a number of fixes and improvements to its statistics management and collection; it now operates its calendar in GMT and ensures that for checkin purposes it always reports one day and only one complete day to the checkin service.

    Also change the checkin option from "-c" to "--checkin" since it is really a special thing.