commit 44cf67accdc294f46bf82b3f8661f353dc5e22f0
Author: Vincent Petry <pvince81@owncloud.com>
Date:   Mon Sep 19 12:17:06 2016 +0200

    Storage 503 message improvements

    "Storage not available" is now "Storage temporarily not available".
    Exceptions are now logged in DEBUG level, not FATAL.