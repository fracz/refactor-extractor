commit 587f1807c2cc0cf6cef0c3b39aec52902f19ffa3
Author: Gian Merlino <gianmerlino@gmail.com>
Date:   Tue Sep 12 20:24:45 2017 -0700

    Try to improve CI reliability. (#4787)

    * Try to improve CI reliability.

    Unset _JAVA_OPTIONS just in case it is causing us to use more memory than expected.

    * Replace for loop with foreach.