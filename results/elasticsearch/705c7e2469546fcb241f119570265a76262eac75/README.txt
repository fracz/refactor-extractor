commit 705c7e2469546fcb241f119570265a76262eac75
Author: Shay Banon <kimchy@gmail.com>
Date:   Sun Apr 6 23:39:51 2014 +0200

    Recycled bytes in http + rest layer refactoring phase 2
    Refactor the rest layer handlers to simplify common code paths (like handling) failures, and introduce optional (enabled for netty) rest channel bytes recycling