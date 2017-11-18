commit 2f5ca7706f3d9a8a7e32e8bb2f689cf763374836
Author: Greg Estren <gregce@google.com>
Date:   Fri Jun 17 00:02:06 2016 +0000

    Adds cycle detection errors when top-level dynamic
    configuration creation fails because transitive fragment
    visitation hits these cycles.

    This makes CircularDependencyTest pass with dynamic
    configurations.

    It's a little bit unfortunate that BuildViewTestCase
    follows a different code path to create configured targets
    than production (BuildView.getConfiguredTargetForTesting
    vs. BuildView.update). As a result, doing an actual build over
    the rules defined in CircularDependencyTest#testTwoCycles
    correctly reports the cycle, while the test itself doesn't.
    That means the test isn't 100% faithfully testing production
    logic.

    But I'm not interested in fixing the gap between
    BuildView.update and BuildView.getConfiguredTargetForTesting
    in this change. That's part of a larger refactoring effort
    on the various forked ways of acccessing configured targets
    and dependencies in BuildView.

    --
    MOS_MIGRATED_REVID=125118553