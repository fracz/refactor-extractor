commit 2f7d60f234f6b1fb47ffc9e79259d1da00834405
Author: Norman Maurer <nmaurer@redhat.com>
Date:   Fri May 2 09:52:59 2014 +0200

    Minimize memory footprint of HashedWheelTimer and context-switching

    Motivation:
    At the moment there are two issues with HashedWheelTimer:
    * the memory footprint of it is pretty heavy (250kb fon an empty instance)
    * the way how added Timeouts are handled is inefficient in terms of how locks etc are used and so a lot of context-switching / condition can happen.

    Modification:
    Rewrite HashedWheelTimer to use an optimized bucket implementation to store the submitted Timeouts and a MPSC queue to handover the timeouts.  So volatile writes are reduced to a minimum and also the memory foot-print of the buckets itself is reduced a lot as the bucket uses a double-linked-list. Beside this we use Atomic*FieldUpdater where-ever possible to improve the memory foot-print and performance.

    Result:
    Lower memory-footprint and better performance