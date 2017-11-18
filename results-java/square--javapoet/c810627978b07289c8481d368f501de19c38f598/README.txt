commit c810627978b07289c8481d368f501de19c38f598
Author: Christian Stein <sormuras@gmail.com>
Date:   Thu Jan 14 08:16:20 2016 +0100

    Literal conversion clean up.

    Character and String literal conversion refactored.
    Made AnnotationSpec.Builder.addMemberForValue package private.
    Refactored and fixed bug in AnnotationSpec.get(AnnotationMirror).
    Removed redundant public modifiers from package private Util methods.