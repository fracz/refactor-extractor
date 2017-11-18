commit 063527f3e44d080bf19d99ee030c6f11f3452df0
Author: Tobias Lindaaker <tobias@thobe.org>
Date:   Mon Sep 1 10:45:39 2014 +0200

    Fixes race condition in test code.

    RemoteRequestMonitoringIT would fail under certain cicrumstances
    due to EideticRequestMonitor not being thread safe.

    This diff also improves the failure reporting on the same test case.