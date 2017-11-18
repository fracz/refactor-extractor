commit 9192219f13a38ec8b9ec67192ba0b8797c45cf82
Author: Boaz Leskes <b.leskes@gmail.com>
Date:   Thu Nov 6 11:21:56 2014 +0100

    Discovery: don't wait joinThread when stopping

    When a node stops, we cancel any ongoing join process. With #8327, we improved this logic and wait for it to complete before shutting down the node. However, the joining thread is part of a thread pool and will not stop until the thread pool is shutdown.

    Another issue raised by the unneeded wait is that when we shutdown, we may ping ourselves - which results in an ugly warn level log. We now log all remote exception during pings at a debug level.

    Closes #8359