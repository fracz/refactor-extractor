commit 85a25bdf7384ad3fb864c1589ef9d3ebe7b80917
Author: Robert Muir <rmuir@apache.org>
Date:   Tue Jul 7 20:11:50 2015 -0400

    harden logic around integ test workspace and process mgmt

    there is more to do here, but this is already a lot more robust.

    * don't clean workspace in teardown, it might be useful for debugging if stuff fails.
    * kill ES/clean workspace in setup, so things always work even in the case of ^C
    * use pidfile to kill
    * fail if kill errors
    * refactor a bit more logic here