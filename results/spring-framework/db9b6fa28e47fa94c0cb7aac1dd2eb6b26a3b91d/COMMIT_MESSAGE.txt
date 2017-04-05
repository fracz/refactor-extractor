commit db9b6fa28e47fa94c0cb7aac1dd2eb6b26a3b91d
Author: Brian Clozel <bclozel@gopivotal.com>
Date:   Wed Jan 22 10:29:57 2014 +0100

    Split tests for MethodMessageHandlers impl.

    Prior to this commit, all MethodMessageHandlers tests were
    implemented in a single class. Since SimpAnnotationMsgHandler
    has been refactored with an abstract class, tests also
    needed such a refactoring.

    This commit creates test fixtures for AbstractMethodMessageHandler.

    Issue: SPR-11191