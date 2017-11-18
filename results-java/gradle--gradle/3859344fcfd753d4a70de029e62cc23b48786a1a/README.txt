commit 3859344fcfd753d4a70de029e62cc23b48786a1a
Author: Stefan Wolf <wolf@gradle.com>
Date:   Fri Oct 28 15:53:42 2016 +0200

    Use only baseVersion in jar files

    Another change to improve caching of the Gradle build.
    This is due to various places where the name of the jar file is used
     - classpath in the manifest of the launcher
     - name of jar files in the distribution

    PR #798