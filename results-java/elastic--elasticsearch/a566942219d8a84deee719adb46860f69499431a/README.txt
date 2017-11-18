commit a566942219d8a84deee719adb46860f69499431a
Author: Jason Tedor <jason@tedor.me>
Date:   Mon Oct 30 13:10:20 2017 -0400

    Refactor internal engine

    This commit is a minor refactoring of internal engine to move hooks for
    generating sequence numbers into the engine itself. As such, we refactor
    tests that relied on this hook to use the new hook, and remove the hook
    from the sequence number service itself.

    Relates #27082