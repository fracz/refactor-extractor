commit 8187f1e78415e7eda2c94890ee635b3e49fca161
Author: Carl Mastrangelo <notcarl@google.com>
Date:   Mon Oct 16 17:43:40 2017 -0700

    util: improve scalability of RR load balancer

    In relative order of importance:

    * Don't acquire lock when picking subchannel.
    * Use O(1) lookup for updating channel state
    * Use non synchronized ref instead of AtomicReference
    * Dont store size in picker.
    * make class final
    * remove test that was not valid