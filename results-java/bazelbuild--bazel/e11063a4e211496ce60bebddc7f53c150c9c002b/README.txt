commit e11063a4e211496ce60bebddc7f53c150c9c002b
Author: Ulf Adams <ulfjack@google.com>
Date:   Thu Dec 15 17:33:32 2016 +0000

    Move TestStrategy to lib.exec package.

    This is part of refactoring test strategy to unify its implementation between
    Bazel and Blaze (Google's internal version of Bazel), which should fix several
    issues in Bazel.

    It's also necessary to untangle lib.rules and lib.exec to enforce proper
    layering by separating compilation of these packages, and to provide a minimal
    Bazel binary. In particular, no core part of Bazel should depend on any of the
    rules, to facilitate moving them out of Bazel / reimplementing them in Skylark
    (except for some core rules like test_suite, alias, and genquery).

    --
    PiperOrigin-RevId: 142151901
    MOS_MIGRATED_REVID=142151901