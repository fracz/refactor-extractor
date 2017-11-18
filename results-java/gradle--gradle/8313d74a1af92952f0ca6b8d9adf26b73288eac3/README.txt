commit 8313d74a1af92952f0ca6b8d9adf26b73288eac3
Author: Lóránt Pintér <lorant@gradle.com>
Date:   Fri Oct 28 01:39:00 2016 +0200

    Use same hashing for classloaders as for `@Classpath` task properties

    This removes some duplicate implementation, and also fixes a problem where we ignored the full path of every file on a classloader's classpath. Future improvements made to `@Classpath` property hashing will also help make classloader hashing better.

    Currently we create two instances of `DefaultClasspathSnapshotter`: one in the global scope and one for task execution scope. These can be merged into one service once we have a global file snapshotting service.

    +review REVIEW-6316