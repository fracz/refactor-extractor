commit 35f72be50b8a2d11bce591dcdac5dc3fa336dac0
Author: Dianne Hackborn <hackbod@google.com>
Date:   Mon Sep 16 10:57:39 2013 -0700

    Implement issue #10691359: Kill long-running processes

    We now have the activity manager kill long-running processes
    during idle maintanence.

    This involved adding some more information to the activity manager
    about the current memory state, so that it could know if it really
    should bother killing anything.  While doing this, I also improved
    how we determine when memory is getting low by better ignoring cases
    where processes are going away for other reasons (such as now idle
    maintenance).  We now won't raise our memory state if either a process
    is going away because we wanted it gone for another reason or the
    total number of processes is not decreasing.

    The idle maintanence killing also uses new per-process information
    about whether the process has ever gone into the cached state since
    the last idle maintenance, and the initial pss and current pss size
    over its run time.

    Change-Id: Iceaa7ffb2ad2015c33a64133a72a272b56dbad53