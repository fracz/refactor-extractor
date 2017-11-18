commit b88cb35ea231fffa807a9f1d97b6b38e412bb34b
Author: Stephane Nicoll <snicoll@pivotal.io>
Date:   Mon Sep 12 15:59:18 2016 +0200

    Fix JMS health indicator

    This commit improves the JMS health indicator to identify a broken broker
    that uses failover. An attempt to start the connection is a good way to
    make sure that it is effectively available.

    Closes gh-6818