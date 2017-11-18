commit 274d39e8ec184cdd6e70a856364c48d850ece4e5
Author: Adam Murdoch <adam@gradle.com>
Date:   Mon Sep 19 11:22:09 2016 +1000

    Change file collection snapshotter to cache file type lookup in-memory, and use this to avoid repeated lookup of the same file.

    The caching is applied only to the 'root' elements of a file collection, and is not applied to files nested within a directory. The caching uses a very simple strategy to invalidate the cached entries, specifically discards all entries when a task _action_ is executed. This means the caching has value only when the build is mostly or completely up-to-date and is dominated by tasks that take files instead of directories as input, such as a native build.

    This is simply an initial step to avoid unnecessary work during incremental build, and the caching will be improved in subsequent changes.