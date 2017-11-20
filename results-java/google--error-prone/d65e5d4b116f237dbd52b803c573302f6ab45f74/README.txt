commit d65e5d4b116f237dbd52b803c573302f6ab45f74
Author: cushon <cushon@google.com>
Date:   Wed Oct 4 17:18:07 2017 -0700

    Add a refactoring to canonicalize durations

    RELNOTES: New check to canonicalize Durations, e.g. `Duration.ofDays(1)` instead of `Duration.ofSeconds(86400)`.

    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=171088261