commit 055d5ae0ee8609841ca059a693eff34c054ad9fe
Author: Joseph Lenton <josephlenton@studiofortress.com>
Date:   Fri Mar 15 18:30:40 2013 +0000

    simplified the code

    The twilight theme has been removed from the JavaScript,
    and instead provided through the CSS script.
    This has reduced the amount of code, and should improve
    rendering a tad.

    The editor is also now padded in now on the left side,
    and close matches how it was before ace was added.

    The line heights of the h2 elements,
    is now fixed (before they were being set the 18px).