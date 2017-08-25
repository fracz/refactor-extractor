commit 42eb3f57c38ad169c9e33a7f37cc6d164ab6f368
Author: JDGrimes <jdg@codesymphony.co>
Date:   Fri Jun 5 16:52:06 2015 -0400

    Use a static property for list and methods as keys

    This lets us merge the two lists of methods just once, and use
    `isset()` instead of `is_array()`, both of which provide improved
    performance.