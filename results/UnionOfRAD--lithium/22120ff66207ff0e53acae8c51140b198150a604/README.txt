commit 22120ff66207ff0e53acae8c51140b198150a604
Author: David Persson <davidpersson@gmx.de>
Date:   Sun Jan 12 01:49:30 2014 +0100

    Optimizing $_FILES parsing in action request.

    Check if parsing is required and only than parse and merge.

    Benchmarks show an improvement of 0.6-2.4% in CPU time in very simple
    scenarios without triggering the $_FILES parsing when instantiating
    the request object.