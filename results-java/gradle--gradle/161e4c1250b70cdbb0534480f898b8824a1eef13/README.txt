commit 161e4c1250b70cdbb0534480f898b8824a1eef13
Author: Rene Groeschke <rene@breskeby.com>
Date:   Thu Dec 6 20:31:33 2012 +0100

    GRADLE-2585: Avoid multiple lookups of maven-metadata.xml when resolving snapshot versions by using an extra attribute for storing timestamped snapshot version number.

    TODO: - further planned refactorings will use ModuleSource for passing the timestamped versions