commit c8fec4c6fe1e2a521a12a93ab55018091773402f
Author: Jacob Hansson <jakewins@gmail.com>
Date:   Wed Jan 20 07:39:05 2016 -0600

    Improved deadlock resolution strategy for enterprise lock manager.

    When a deadlock is detected, it's rather likely that multiple participants
    in the deadlock realize at the same time that they are deadlocking. Because
    we want to minimize aborts, we have code that chooses which of the
    deadlocking parties should bail out.

    This code is very sensitive, because if there is ever a code path where both
    parties think the other will abort, we would end up in a real deadlock,
    rather than averting it.

    Currently, the strategy has been to look at wait lists - the client with the
    highest number of waiting clients is allowed to continue, while the other
    client aborts. However, this strategy is sub-optimal, partially because
    the data we have on number of waiting clients is unreliable, but mainly
    because there was no good tie breaker if both clients have the sized wait
    lists - if both clients have the same number of clients waiting for them,
    both will abort.

    This modifies this, while hopefully making the code a bit clearer:

     - There is now a tie breaker, if both clients are on the same "level"
       we abort based on client id, ensuring only one client aborts.
     - There is now two strategies, chosen with a feature toggle, to allow
       real-world trialing of the two prominent deadlock resolution strategies.
       The two strategies introduced are both based on counts of locks held,
       rather than counts of current wait lists:

       - ABORT_YOUNG, assumes that higher throughput can be achieved by letting
                      transactions with more locks held (presumed older) finish.
                      Young transactions with fewer locks abort.
       - ABORT_OLD, assumes old monolithic transactions are "holding up the
                    line", and that higher throughput can be achieved by
                    aborting transactions with more locks.

    This sets a nice precedent for introducing transaction priorities, where we
    could choose to abort transactions with lower priorities. Something to
    consider for future improvements.