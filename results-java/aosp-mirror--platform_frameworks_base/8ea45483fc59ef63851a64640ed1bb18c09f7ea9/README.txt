commit 8ea45483fc59ef63851a64640ed1bb18c09f7ea9
Author: Erik Kline <ek@google.com>
Date:   Mon Feb 13 17:28:53 2017 +0900

    Cleanup in the face of upstream error

    If either enableNat() or startInterfaceForwarding() fail, be sure
    to cleanup any commands that might have succeeded.

    Most of this change is a refactoring of cleanupUpstreamIface() into
    two methods, one of which (cleanupUpstreamInterface()) is reused
    in error handling scenarios.

    Test: as follows
        - built (bullhead)
        - flashed
        - booted
        - runtest -x .../tethering/TetherInterfaceStateMachineTest.java passes
    Bug: 32031803
    Bug: 32163131

    Change-Id: Ia4d56e03beeab1908d8b8c2202e94992f1aa58a4