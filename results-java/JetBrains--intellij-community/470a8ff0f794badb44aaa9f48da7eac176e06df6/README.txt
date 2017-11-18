commit 470a8ff0f794badb44aaa9f48da7eac176e06df6
Author: Denis.Zhdanov <Denis.Zhdanov@jetbrains.com>
Date:   Fri Oct 28 14:04:49 2011 +0400

    IDEA-75726 Gradle: Allow to adjust project settings prior to importing

    1. Dependencies model is refactored in order to share common functionality at the base class;
    2. Added ability to adjust module dependency settings prior to importing;