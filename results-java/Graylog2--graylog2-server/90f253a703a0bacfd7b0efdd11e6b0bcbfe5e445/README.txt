commit 90f253a703a0bacfd7b0efdd11e6b0bcbfe5e445
Author: Kay Roepke <kroepke@googlemail.com>
Date:   Thu Oct 23 17:03:40 2014 +0200

    netty based transports erroneously shared all their channelhandlers after refactoring.

    the channelpipeline factory always received the same instance from subclasses, because they were directly instantiated.
    wrap each of them in a callable, for actually shared ones use Callables.returning() for convenience.

    this affected only the http and tcp based transports