commit 906497c574d45d8dfd295b16dece0d0bc32c0895
Author: Dianne Hackborn <hackbod@google.com>
Date:   Mon May 10 15:57:38 2010 -0700

    Hopefully fix issue #2662536: Why is launcher being killed?

    It looks like there was a subtle bug where Process.setOomAdj() could
    return false just because the given process doesn't exist, even though
    it is documented to only return false if OOM killing is not supported
    at all.  This would cause the activity manager to fall into its code
    path of trying to clean up processes itself, which it does a much
    poorer problem at.  I am thinking we may be seeing this problem more
    now that the activity manager is killing background processes itself
    when there are too many of them.

    In addition, this change cleans up and reduces some of the logging
    around killing processes.

    Finally, try to improve process LRU management a bit by taking
    into account process dependencies.  Any dependent processes are
    pulled up in the LRU list with the processes that is actually
    moving.  Also, we bring a process up if someone accesses its content
    provider.

    Change-Id: I34ea161f839679345578ffe681e8d9c5d26ab948