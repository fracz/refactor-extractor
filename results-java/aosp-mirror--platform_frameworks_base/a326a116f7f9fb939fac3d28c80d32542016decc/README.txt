commit a326a116f7f9fb939fac3d28c80d32542016decc
Author: Tim Murray <timmurray@google.com>
Date:   Tue Aug 25 00:28:37 2015 -0700

    Enable migration to big cores for app launches.

    Uses cpusets to move all foreground tasks to the big cores in order
    to improve overall app launch latency. Big cores will be used for
    three seconds, and then the cpuset assignment is reset, allowing
    foreground tasks to fall back to the little cores as appropriate.

    Associated system/core and device/* changes in order to enable
    the boost cpuset and configure it per-device.

    bug 21915482

    Change-Id: Id8a0efcb31950c1988f20273ac01c89c8c948eaf