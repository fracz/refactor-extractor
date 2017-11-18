commit fd65813157e4dd7fa9f0b7c5dd4c8f536cc6316a
Author: Jeff Sharkey <jsharkey@android.com>
Date:   Wed May 3 11:38:01 2017 -0600

    Offer to wait until broadcasts have drained.

    We've seen evidence of lab devices racing with other apps that are
    using cache space immediately after tests wipe it clean, which can
    cause test failures.  To mitigate this, try our best to wait for the
    device to go "idle" by watching for broadcast queues to fully drain.

    Also improve javadocs along the way.

    Test: cts-tradefed run commandAndExit cts-dev -m CtsAppSecurityHostTestCases -t android.appsecurity.cts.StorageHostTest
    Bug: 37486230, 37566983, 37913442, 37914374
    Change-Id: I4d430db443b6fa6d33a625fe07b90279b5d51c12