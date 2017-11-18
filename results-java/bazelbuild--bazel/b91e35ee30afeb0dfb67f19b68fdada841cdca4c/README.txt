commit b91e35ee30afeb0dfb67f19b68fdada841cdca4c
Author: Nathan Harmata <nharmata@google.com>
Date:   Fri Dec 16 02:39:54 2016 +0000

    Some improvements to ParallelQueryUtils.

    (i) Use a CountDownLatch in ParallelQueryUtils#executeQueryTasksAndWaitInterruptibly to avoid busy-looping while waiting for query subtask completion (this busy-looping unnecessarily ties up a thread). But we still retain the fail-fast semantics we want (I renamed the method to emphasize this).

    (ii) Also have a special-case in ParallelQueryUtils#executeQueryTasksAndWaitInterruptibly for evaluating one query subtask so we don't wastefully use another thread.

    (iii) Also add ThreadSafety annotations to ParallelQueryUtils.

    ----

    (i) and (ii) combine to address the following theoretical issue. Suppose we're evaluating a query expression of the form "(e1 - e2) + (e3 - e4)". The old code would (with the worst-case FJP thread scheduling) have the following threads at the _same_ time:

    Main QueryCommand thread - executeQueryTasksAndWaitInterruptibly(queryTasks = [(e1 - e2), (e3 - e4)]
    FJP thread - executeQueryTasksAndWaitInterruptibly(queryTasks = [e2])
    FJP thread - eval(e2)
    FJP thread - executeQueryTasksAndWaitInterruptibly(queryTasks = [e4])
    FJP thread - eval(e4)

    So of those 5 concurrent threads, 3 would be doing busy-loop waiting. For more pathological query expressions, we could end up tying up lots of threads doing wasteful busy-loops.

    --
    PiperOrigin-RevId: 142215680
    MOS_MIGRATED_REVID=142215680