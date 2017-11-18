commit 766299326735ae43331bdb427d8287185ca56760
Author: Denis.Zhdanov <Denis.Zhdanov@jetbrains.com>
Date:   Wed Feb 15 18:06:40 2012 +0400

    IDEA-76142: Gradle support - cannot update IDEA projects once one of build.gradle files changes

    1. 'entity id' is introduced and 'entity <--> id' mapping is provided;
    2. Repackaging at the gradle model classes;
    3. Added fine-grained infrastructure for importing gradle-local changes;
    4. Added topological sorting of the target gradle project structure nodes;
    5. Tests are refactored in order to provide high-level gradle-local IoC container;
    6. 'Import entity' test infrastructure is added and couple of tests are provided;