commit 826f3d11165e36c5a165c161a5238801ab572d03
Author: Daniel Lacasse <daniel@gradle.com>
Date:   Fri Mar 10 12:04:06 2017 -0500

    Fix 3 bugs in the parallel console

    1- The line wrapping wasn't detected properly which cause line
    wrapping logic to fire when the line was at the terminal max width.
    2- A bad assumption made the text area cursor pass the end of the
    terminal hence offsetting the output and causing text to be
    overwritten.
    3- Improvement to the beforeNewLineWritten callback to support line
    wrapping and receive write cursor representing what is seen on the
    terminal

    Unit tests were also improved to catch those particular scenarios.