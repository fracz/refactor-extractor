commit 970510b9c5299d480b3ec7f8403afd8265456f87
Author: Dianne Hackborn <hackbod@google.com>
Date:   Wed Feb 24 16:56:42 2016 -0800

    Work on issue #26390161: Throttle syncs/jobs when system is low on RAM

    We now have a fixed array of job service contexts, which doesn't
    vary by build configuration.  Instead, we keep track of the maximum
    number of concurrent jobs we want to allow to run, and don't
    make use of a context if it would put us over that limit.

    The available contexts is now 8 (the largest used to be 6), although
    the maximum we will normally schedule is still 6.  We have the other
    two around only for use by the current foreground app, to allow it
    to schedule work while the user is in it, even if we have reached
    our normal limit on the number of concurrent jobs.

    The maximum number of concurrent jobs varies based on the memory
    state of the device, from 6 (if memory is normal) down to 1
    (if memory is critical).  We aren't yet trying to stop all jobs
    if memory gets lower than critical.

    Instead of just keeping track of whether a uid is in the foreground,
    we now track whether it is the top as well.  Only the top uid
    can schedule additional jobs above the current limit.

    Also improved some of the dumpsys output.

    Change-Id: Icc95e42231a806f0bfa3e2f99ccc2b85cefac320