commit 5b2dab3ad9b2226f5be972f6b4035f82360d1199
Author: Ben Christensen <benjchristensen@netflix.com>
Date:   Mon Dec 30 22:35:49 2013 -0800

    ExecutorScheduler Memory Leak Fix

    - new InnerExecutorScheduler and childSubscription
    - improvements to unit tests