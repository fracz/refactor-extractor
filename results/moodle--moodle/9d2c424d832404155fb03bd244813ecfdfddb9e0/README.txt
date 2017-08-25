commit 9d2c424d832404155fb03bd244813ecfdfddb9e0
Author: David Mudrak <david@moodle.com>
Date:   Sat Dec 3 14:11:26 2011 +0100

    MDL-30340 Block stickiness computation improvements

    This is an attempt to clean up and fix the computation of the block
    stickiness. At first, the page pattern can't be ignored because the user
    may want to currently try to limit the page pattern. Second, the
    site-index pattern can be forced only if the user selected 'Front page
    only' as the page context.