commit 2b3d732f05e22081dc0b7c6f753e3b3e8d0cf047
Author: Denis.Zhdanov <Denis.Zhdanov@jetbrains.com>
Date:   Thu Feb 16 15:39:36 2012 +0400

    IDEA-76142: Gradle support - cannot update IDEA projects once one of build.gradle files changes

    1. Gradle node descriptor uses only entity id as a data value now;
    2. Minor refactorings;