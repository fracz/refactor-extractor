commit 5531e807248e3155e7509bea9c1adcc58592157e
Author: Sebastien Deleuze <sdeleuze@pivotal.io>
Date:   Mon Aug 8 11:13:17 2016 +0200

    Anticipate reactor.test.TestSubscriber removal

    reactor.test.TestSubscriber will not be part of Reactor Core
    3.0.0 since it needs to be refactored to fit all the needs
    expressed by the users. It is likely to be back later in one
    of the Reactor Core 3.0.x releases.

    This commit anticipate this removal by temporarily copying
    TestSubscriber in spring-core test classes. As soon as
    the new TestSubscriber will be available in Reactor Core,
    Spring Framework reactive tests will use it again.