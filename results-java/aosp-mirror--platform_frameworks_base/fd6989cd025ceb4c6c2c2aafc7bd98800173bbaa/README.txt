commit fd6989cd025ceb4c6c2c2aafc7bd98800173bbaa
Author: Riley Andrews <riandrews@google.com>
Date:   Tue Aug 19 23:46:31 2014 -0700

    Use the crop function in the surfaceflinger screenshot api.

    - By having surfaceflinger crop the screenshot instead of
      WindowManagerService, this greatly reduces the size of
      of temporary screenshot buffers that tablets have to
      allocate.
    - App to home, App to recents times are improved noticeably.

    Change-Id: Iff889cc8c57b157778fb16993a71546fd9abfc00
    Bug 17006948
    Bug 16987565