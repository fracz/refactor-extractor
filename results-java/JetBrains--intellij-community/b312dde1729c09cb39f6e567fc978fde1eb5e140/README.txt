commit b312dde1729c09cb39f6e567fc978fde1eb5e140
Author: irengrig <Irina.Chernushina@jetbrains.com>
Date:   Mon Nov 28 14:45:35 2011 +0400

    IDEA-56157 Sort out performance problem with Perforce changelists refresh
    IDEA-67375 Files periodically loose their Perforce state

    P4 local changes provider refactored.

    1)We calculated read-only unversioned only on start; after that, we listen to local changes and invalidate changes
    2) [for big amount of edited files] when asking "p4 have" for changed files (to get to know their revisions), keep have commands limited by number of arguments