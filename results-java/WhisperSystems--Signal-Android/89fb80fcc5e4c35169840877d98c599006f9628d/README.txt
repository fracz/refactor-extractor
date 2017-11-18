commit 89fb80fcc5e4c35169840877d98c599006f9628d
Author: Jake McGinty <me@jake.su>
Date:   Tue Sep 16 16:21:41 2014 -0700

    MmsConnection refactor

    - Use Apache HttpClient v4.x, only library that seems to like HTTP proxies
    - Remove custom redirect logic in favor of library's

    Fixes #1904
    // FREEBIE