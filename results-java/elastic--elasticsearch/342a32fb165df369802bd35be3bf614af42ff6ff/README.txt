commit 342a32fb165df369802bd35be3bf614af42ff6ff
Author: Shay Banon <kimchy@gmail.com>
Date:   Sun May 4 03:08:50 2014 +0200

    Search might not return on thread pool rejection
    When a thread pool rejects the execution on the local node, the search might not return.
    This happens due to the fact that we move to the next shard only *within* the execution on the thread pool in the start method. If it fails to submit the task to the thread pool, it will go through the fail shard logic, but without "counting" the current shard itself. When this happens, the relevant shard will then execute more times than intended, causing the total opes counter to skew, and for example, if on another shard the search is successful, the total ops will be incremented *beyond* the expectedTotalOps, causing the check on == as the exit condition to never happen.
    The fix here makes sure that the shard iterator properly progresses even in the case of rejections, and also includes improvement to when cleaning a context is sent in case of failures (which were exposed by the test).
    Though the change fixes the problem, we should work on simplifying the code path considerably, the first suggestion as a followup is to remove the support for operation threading (also in broadcast), and move the local optimization execution to SearchService, this will simplify the code in different search action considerably, and will allow to remove the problematic #firstOrNull method on the shard iterator.
    The second suggestion is to move the optimization of local execution to the TransportService, so all actions will not have to explicitly do the mentioned optimization.
    fixes #4887