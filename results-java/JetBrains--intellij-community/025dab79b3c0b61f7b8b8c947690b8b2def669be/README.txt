commit 025dab79b3c0b61f7b8b8c947690b8b2def669be
Author: Kirill Likhodedov <Kirill.Likhodedov@jetbrains.com>
Date:   Sun Nov 11 17:01:29 2012 +0400

    Git Push: refactor GitOutgoingCommitsCollector to use QueueProcessor

    Simplify the implementation by getting rid of manual queue processing and state watching.

    Changes in the logic:
     - remove "refresh" parameter from waitForCompletionAndGetCommits: if refresh will be needed by client, he can call collect() and then wait...().
     - the queue can't be empty, because the push dialog always requests collect() on dialog show (commit & push will do the same in the "no dialog" operation mode). Anyway, since the Collector holds the changes from the previous invocation, check for EMPTY didn't actually check anything.