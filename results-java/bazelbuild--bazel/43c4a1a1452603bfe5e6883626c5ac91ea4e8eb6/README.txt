commit 43c4a1a1452603bfe5e6883626c5ac91ea4e8eb6
Author: Philipp Wollermann <philwo@google.com>
Date:   Tue Aug 25 12:52:57 2015 +0000

    Execute spawns inside sandboxes to improve hermeticity (spawns can no longer use non-declared inputs) and safety (spawns can no longer affect the host system, e.g. accidentally wipe your home directory). This implementation works on Linux only and uses Linux containers ("namespaces").

    The strategy works with all actions that Bazel supports (C++ / Java compilation, genrules, test execution, Skylark-based rules, ...)  and in tests, Bazel could successfully bootstrap itself and pass the whole test suite using sandboxed execution.

    This is not the default behavior yet, but can be activated explicitly by using:
    bazel build --genrule_strategy=sandboxed --spawn_strategy=sandboxed //my:stuff

    --
    MOS_MIGRATED_REVID=101457297