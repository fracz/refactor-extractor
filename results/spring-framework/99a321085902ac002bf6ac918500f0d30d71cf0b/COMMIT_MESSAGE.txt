commit 99a321085902ac002bf6ac918500f0d30d71cf0b
Author: Brian Clozel <bclozel@pivotal.io>
Date:   Tue Oct 18 09:06:33 2016 +0200

    Refactor tests with ScriptedSubscriber

    Reactor recently added the `ScriptedSubscriber` in its new
    `reactor-addons` module. This `Subscriber` revissits the previous
    `TestSubscriber` with many improvements, including:

    * scripting each expectation
    * builder API that guides you until the final verification step
    * virtual time support

    This commit refactor all existing tests to use this new
    infrastructure and removed the `TestSubscriber` implementation.

    Issue: SPR-14800