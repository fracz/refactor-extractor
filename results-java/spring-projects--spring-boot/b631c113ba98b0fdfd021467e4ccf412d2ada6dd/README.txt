commit b631c113ba98b0fdfd021467e4ccf412d2ada6dd
Author: Andy Wilkinson <awilkinson@gopivotal.com>
Date:   Mon Nov 4 11:01:52 2013 +0000

    Update AetherGrapeEngine to honour --local

    When running an application, --local can be used to collect the
    application's dependencies in a local directory. Prior to
    AetherGrapeEngine being introduced, using --local would result in the
    dependencies being written to ./grapes. When AetherGrapeEngine was
    introduced --local no longer had any effect.

    This commit updates AetherGrapeEngine so that it honours --local,
    writing its dependencies to ./repository. When --local is not specified
    dependencies are written to ~/.m2/repository (the standard location
    for the local Maven cache). As part of this change TestCommand has
    been refactored so that it lazily initialises its GroovyCompiler. This
    ensures that RunCommand has a chance to set the system property that
    backs --local before AetherGrapeEngine is initialised and accesses the
    property.

    Fixes #99