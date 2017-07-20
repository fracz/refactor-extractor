commit bca649230904d4ac91567f0aa361cf88235edd44
Author: mark_story <mark@mark-story.com>
Date:   Sat Oct 15 16:40:11 2011 -0400

    Update debug()

    debug() now uses Debugger to output content and generate traces.
    This improves the readability of its output as null/true/false are better
    represented.  Objects are easier to interpret as well.