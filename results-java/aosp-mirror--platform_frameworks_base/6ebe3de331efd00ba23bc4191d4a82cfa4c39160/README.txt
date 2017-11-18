commit 6ebe3de331efd00ba23bc4191d4a82cfa4c39160
Author: Chet Haase <chet@google.com>
Date:   Mon Jun 17 16:50:50 2013 -0700

    Fix transitions on disappearing view hiearchies

    Previously, Fade transitions did not work correctly on hirearchies; they
    only handled individual views. in particular, they would side-effect all
    fading views by removing them from their parent to fade them out in the
    overlay of the scene root. This worked for the fade-out transition itself,
    but caused problems when those same hierarchies were added back in and
    another Fade was run on the hierarchy, because now all of the views inside
    that parent node had been removed, so they didn't fade in at all.

    The fix was to add logic in Visibility to detect when a disappearing
    view was inside a hierarchy that was also disappearing, and to skip the
    fade on the views inside that hierarchy, leaving only the top-most
    disappearing view to be faded out, thus preserving the hierarchy under
    that faded-out group.

    Along the way, there were various cleanups, fixes, and refactorings in the
    transition code, and slight API modifications.

    Issue #9406371 Transitions: Removing view hierarchy not working correctly
    Issue #9470255 Transitions: Separate different transitions by Scene Root

    Change-Id: I42e80dac6097fee740f651dcc0535f2c57c11ebb