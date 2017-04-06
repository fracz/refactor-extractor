commit 30c6f2fa23c9db62acdbc7d7bfb643e8182c4d67
Author: Simon Willnauer <simonw@apache.org>
Date:   Tue Dec 10 18:55:07 2013 +0100

    Improve RoutingNodes API

    Currently the RoutingNodes API allows modification of it's internal state outside of the class.
    This commit improves the APIs of `RoutingNode` and `RoutingNode` to change internal state
    only within the classes itself.

    Closes #4458