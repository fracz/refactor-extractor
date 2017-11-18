commit d23b7fbd47850ee7e8c9c5987eff328c0721d206
Author: Andrew Pellegrini <apell@google.com>
Date:   Mon Aug 31 21:08:30 2015 +0000

    Switches AndroidRobolectricTest to using .aars to provide transitive resources to the test runner instead of ResourceContainers. Update AndroidLibraryAarProvider to contain transitive closure of .aars. Provides an ~4x speed improvement in test startup time.

    NEW: Switched to ordered maps in CompositeLibraryAndroidManifestLocator to prevent manifest ordering flakiness bug. Switched to ImmutableSetMultimap in CompositeLibraryAndroidManifestLocator to prevent IllegalArgumentExceptions from duplicate package aliases and added test.

    RELNOTES: android_resources is no longer allowed as a dep for android_robolectric_test.

    --
    MOS_MIGRATED_REVID=101972311