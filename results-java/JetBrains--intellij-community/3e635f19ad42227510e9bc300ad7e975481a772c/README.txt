commit 3e635f19ad42227510e9bc300ad7e975481a772c
Author: Denis.Zhdanov <Denis.Zhdanov@jetbrains.com>
Date:   Sun May 19 11:04:21 2013 +0400

    IDEA-107420 Gradle: Correct tasks representation

    1. Local external system settings are processed now (tool window tree 'expanded' state is correctly stored and restored now; last projects/tasks are cached as well);
    2. Minor refactorings;