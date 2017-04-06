commit 83d9dab79882700fb21cb496282bafdb1d28a5ca
Author: Boaz Leskes <b.leskes@gmail.com>
Date:   Thu Nov 6 11:21:56 2014 +0100

    Discovery: a more lenient wait joinThread when stopping

    When a node stops, we cancel any ongoing join process. With #8327, we improved this logic and wait for it to complete before shutting down the node. In our tests we typically shutdown an entire cluster at once, which makes it very likely for nodes to be joining while shutting down. This introduces a race condition where the joinThread.interrupt can happen before the thread starts waiting on pings which causes shutdown logic to be slow. This commits improves by repeatedly trying to stop the thread in smaller waits.

    Another side effect of the change is that we are now more likely to ping ourselves while shutting down, we results in an ugly warn level log. We now log all remote exception during pings at a debug level.

    Closes #8359