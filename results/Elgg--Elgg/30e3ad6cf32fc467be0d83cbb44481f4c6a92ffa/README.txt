commit 30e3ad6cf32fc467be0d83cbb44481f4c6a92ffa
Author: Steve Clay <steve@mrclay.org>
Date:   Wed Jun 8 23:12:26 2016 -0400

    fix(ui): improves usability of anchors within system messages

    Anchors no longer appear blue on red/green, but instead have the same color
    and are distinguished via underline.

    Clicking an anchor in a message no longer dismisses the message, though
    success messages still will fade after 6 seconds.