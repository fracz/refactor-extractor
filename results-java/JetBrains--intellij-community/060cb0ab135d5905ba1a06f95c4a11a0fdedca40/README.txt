commit 060cb0ab135d5905ba1a06f95c4a11a0fdedca40
Author: Denis Zhdanov <Denis.Zhdanov@jetbrains.com>
Date:   Fri Jun 18 16:31:24 2010 +0400

    IDEA-53596 Soft wrap for editors

    Second soft wraps implementation iteration. Current state:
    * soft wraps are represented at the editor during its repainting;
    * line numbers representations is consistent with soft-wrapped lines presence;
    * logical position representation is consistent with soft-wrapped lines presence;
    * foldings are correctly represented at gutter during soft wraps adding/removing;
    * active line background is drawn correctly in situation when single logical line is sread to multiple visual lines;

    Bugfixes:
    * editor repainting was incorrect during vertical document scrolling;

    Other:
    * green code policy is applied whenever possible. Most of the time these are minor rename refactorings to make spell checker plugin happy;