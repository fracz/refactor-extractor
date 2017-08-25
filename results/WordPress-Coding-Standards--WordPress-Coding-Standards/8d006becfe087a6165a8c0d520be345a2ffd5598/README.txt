commit 8d006becfe087a6165a8c0d520be345a2ffd5598
Author: JDGrimes <jdg@codesymphony.co>
Date:   Wed Jul 29 18:14:38 2015 -0400

    Check spacing of functions

    This adds functions and closures to the control structure spacing
    sniff. The sniff checks for proper spacing on each side of each of the
    parentheses. It also checks that the opening brace is on the same line.

    I’ve actually improved the sniff while I was at it, to check for excess
    space in addition to insufficient space.

    Because of these improvements, and due to the non-configurability of
    the upstream version of this sniff, this sniff is really not a
    duplicate, and so #385 can be closed.

    Fixes #412, #385