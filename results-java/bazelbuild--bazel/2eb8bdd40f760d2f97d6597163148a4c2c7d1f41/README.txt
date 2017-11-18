commit 2eb8bdd40f760d2f97d6597163148a4c2c7d1f41
Author: Dmitry Lomov <dslomov@google.com>
Date:   Wed Apr 6 08:47:30 2016 +0000

    Use the correct Aspect in AspectFunction for Skylark aspects.

    Previously AspectFunction was using an Aspect from the SkyKey, which
    might have been stale.

    This CL fixes the bug as uncovered in the test (see SkylarkAspectsTest),
    but further refactoring is needed since SkylarkAspectClass equals() is
    incorrect, and in fact obtaining the Skylark aspect definition should
    always introduce Skyframe dependency.

    --
    MOS_MIGRATED_REVID=119137636