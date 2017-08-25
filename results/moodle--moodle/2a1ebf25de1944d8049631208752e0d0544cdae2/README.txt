commit 2a1ebf25de1944d8049631208752e0d0544cdae2
Author: Sam Hemelryk <sam@moodle.com>
Date:   Thu Aug 8 10:32:26 2013 +1200

    MDL-41057 csslib: CSS optimiser improvements.

    This commit makes the following changes:
    * Tidies up coding style.
    * Tidies up phpdocs.
    * Improve CSS optimiser unit tests to cover browser hacks.
    * Improve backgound style consolidation.
    * Improve border style consolidation.
    * Fixed optimiser to handle browser hacks like *zoom.
    * Improved @ rule unit tests.
    * Fixed @rule breakages caused by OS specific rules.
    * Added more unit tests to cover a bit more CSS3.

    I am pretty sure this patch will now enable to optimsier to
    work on bootstrap based themes should developers not wish
    to compress thier compiled less CSS.