commit 7bca97bba6ac012f875d758cc5f49546396b20e2
Author: Costin Leau <costin.leau@gmail.com>
Date:   Wed Nov 25 01:04:40 2015 +0200

    HDFS Snapshot/Restore plugin

    Migrated from ES-Hadoop. Contains several improvements regarding:

    * Security
    Takes advantage of the pluggable security in ES 2.2 and uses that in order
    to grant the necessary permissions to the Hadoop libs. It relies on a
    dedicated DomainCombiner to grant permissions only when needed only to the
    libraries installed in the plugin folder
    Add security checks for SpecialPermission/scripting and provides out of
    the box permissions for the latest Hadoop 1.x (1.2.1) and 2.x (2.7.1)

    * Testing
    Uses a customized Local FS to perform actual integration testing of the
    Hadoop stack (and thus to make sure the proper permissions and ACC blocks
    are in place) however without requiring extra permissions for testing.
    If needed, a MiniDFS cluster is provided (though it requires extra
    permissions to bind ports)
    Provides a RestIT test

    * Build system
    Picks the build system used in ES (still Gradle)