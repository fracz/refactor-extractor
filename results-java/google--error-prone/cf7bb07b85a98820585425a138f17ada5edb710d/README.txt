commit cf7bb07b85a98820585425a138f17ada5edb710d
Author: chy <chy@google.com>
Date:   Wed Jul 22 13:39:57 2015 -0700

    Errorprone analyzer for calling query.setKeysOnly() and calling a method on entity that is not needs more properties than a key, and not calling query.setKeysOnly() and only calling methods on entity that only need a key.

    RELNOTES: none

    Results from global refactoring: [] []
    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=98865076