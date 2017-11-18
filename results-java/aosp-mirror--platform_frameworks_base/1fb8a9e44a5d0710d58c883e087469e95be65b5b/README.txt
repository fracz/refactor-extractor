commit 1fb8a9e44a5d0710d58c883e087469e95be65b5b
Author: Chet Haase <chet@google.com>
Date:   Thu Apr 19 09:22:34 2012 -0700

    Fix init of Animation in View drawing code

    The refactor of ViewGroup.drawChild() resulted in an error
    in a new method (View.drawAnimation) where animations were being
    initialized with their view dimensions instead of the parent dimensions.

    Issue #6292681 RotateAnimationTest#testRotateAgainstPoint fails on JRN04
    Issue #6293275 TranslateAnimationTest#testInitialize fails on JRN04

    Change-Id: Ia90711cadd7a6c20fd788e5b8b18a5b28551e68c