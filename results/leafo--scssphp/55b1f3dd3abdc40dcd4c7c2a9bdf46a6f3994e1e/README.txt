commit 55b1f3dd3abdc40dcd4c7c2a9bdf46a6f3994e1e
Author: Anthon Pang <apang@softwaredevelopment.ca>
Date:   Sat Jun 13 12:43:22 2015 -0400

    Fix the test collectors.

    Before: 302 tests, 204 assertions, 117 failures, 94 errors
    After:  302 tests, 256 assertions, 185 failures, 42 errors

    The trim() patch (currently commented out) improves this to:

            302 tests, 256 assertions, 113 failures, 42 errors

    The "compressed" patch (also commented out) further improves this to:

            302 tests, 256 assertions, 111 failures, 42 errors