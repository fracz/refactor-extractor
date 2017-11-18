commit fac8383b7ca371e7e604327569aa5b35bb9576f4
Author: Chris Parsons <cparsons@google.com>
Date:   Wed Jun 8 18:02:03 2016 +0000

    More refactor work on single-/multi- architecture accessor methods of AppleConfiguration.
    Additionally, tweak single-architecture ios-platform logic such that ios_multi_cpus is checked before ios_cpu.

    There are two contexts to note:
      1. Single-architecture logic, (generally post-split), unaware of its own platform type aside
        from configuration. This retrieves platform type from the --apple_platform_type configuration value.
         a. getSingleArchPlatform() for Platform retrieval
         b. getSingleArchitecture() for architecture retrieval
      2. Multi-architecture logic, which should be aware of its own platform type, and passes it into
        configuration accessors.
         a. getMultiArchPlatform(PlatformType)
         b. getMultiArchitectures(PlatformType)

    All callers are migrated to these methods, though some still pass IOS platform type even though
    they may need to be refactored to support additional platform types later.

    --
    MOS_MIGRATED_REVID=124370652