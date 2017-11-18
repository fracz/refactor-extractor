commit 1dae80fdb869837ec070e1d17f4fa424b9f13075
Author: Dain Sundstrom <dain@iq80.com>
Date:   Tue Mar 15 14:21:44 2016 -0700

    Use batch updates for tasks in scheduler

    Scheduler can create many new tasks in a single step.  Instead of
    adding buffers and exchange locations one at a time, update them
    in a single batch for all new tasks.  This improves the scheduling
    time by reducing lock acquisitions.