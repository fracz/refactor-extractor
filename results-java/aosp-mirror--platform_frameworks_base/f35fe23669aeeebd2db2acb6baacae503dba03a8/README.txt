commit f35fe23669aeeebd2db2acb6baacae503dba03a8
Author: Dianne Hackborn <hackbod@google.com>
Date:   Tue Nov 1 19:25:20 2011 -0700

    Add new OOM adjustment for the "previous" process.

    This is the process that you had previously been interacting with
    in the UI before the current one.  Treating it specially should
    allow us to improve the scenario of switching back and forth
    between two apps.

    Also add API constent for ICS MR1.

    Change-Id: Ib3fe4df36b270be11dfd6b7e8d107c9994058a4d