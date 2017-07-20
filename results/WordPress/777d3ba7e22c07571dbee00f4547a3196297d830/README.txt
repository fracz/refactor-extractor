commit 777d3ba7e22c07571dbee00f4547a3196297d830
Author: Scott Taylor <scott.c.taylor@mac.com>
Date:   Fri Jun 19 20:06:25 2015 +0000

    `wptexturize()` improvements:
    * Make sure that strings ending with a number and quotation mark get the proper smart quotes
    * Introduce `wptexturize_primes()`, a logic tree to determine whether or not "7'." represents seven feet, then converts the special char into either a prime char or a closing quote char.

    Adds unit tests.

    Props miqrogroove.
    Fixes #29256.

    Built from https://develop.svn.wordpress.org/trunk@32863


    git-svn-id: http://core.svn.wordpress.org/trunk@32834 1a063a9b-81f0-0310-95a4-ce76da25c4cd