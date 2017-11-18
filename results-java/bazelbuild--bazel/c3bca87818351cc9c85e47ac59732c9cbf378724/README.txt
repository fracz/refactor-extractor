commit c3bca87818351cc9c85e47ac59732c9cbf378724
Author: Andrew Pellegrini <apell@google.com>
Date:   Tue Jul 28 18:16:21 2015 +0000

    Switches AndroidRobolectricTest to using .aars to provide transitive resources to the test runner instead of ResourceContainers. Update AndroidLibraryAarProvider to contain transitive closure of .aars. Provides an ~4x speed improvement in test startup time.

    RELNOTES: android_resources is no longer allowed as a dep for android_robolectric_test.

    --
    MOS_MIGRATED_REVID=99296321