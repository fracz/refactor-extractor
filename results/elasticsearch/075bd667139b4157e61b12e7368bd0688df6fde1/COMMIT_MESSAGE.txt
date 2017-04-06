commit 075bd667139b4157e61b12e7368bd0688df6fde1
Author: mikemccand <mail@mikemccandless.com>
Date:   Tue Aug 26 06:10:55 2014 -0400

    Core: use Java's built-in ConcurrentHashMap

    It's risky to have our own snapshot of Java 8's ConcurrentHashMap:
    unless we keep the sources in sync over time (and OpenJDK's version
    had already diverged), then we won't get bug/performance fixes.  Users
    can choose to upgrade to Java 8 to see the improvements of CHM.

    Closes #7392

    Closes #7296