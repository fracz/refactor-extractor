commit 0865d220f4d55d9dca858710d2f8240b0c87e7d2
Author: Robert Muir <rmuir@apache.org>
Date:   Thu Apr 23 15:04:58 2015 -0400

    Remove crazy permissions for filestores, ssds, now that
    this logic has been refactored.

    Log a warning when security is disabled.