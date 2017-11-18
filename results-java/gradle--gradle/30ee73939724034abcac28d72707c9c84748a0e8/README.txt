commit 30ee73939724034abcac28d72707c9c84748a0e8
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Mon Dec 12 18:37:38 2011 +0100

    Tooling api concurrency - distribution progress loggers...

    -Made sure the distribution progress events are routed to the user's defined listeners. Added concurrent coverage for it.
    -Changed connector services so that they don't share the DistributionFactory.
    -Some refactorings in the concurrent tooling api test case.