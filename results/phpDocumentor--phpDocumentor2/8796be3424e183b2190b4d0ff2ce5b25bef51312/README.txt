commit 8796be3424e183b2190b4d0ff2ce5b25bef51312
Author: Mike van Riel <mike.vanriel@naenius.com>
Date:   Tue Jul 10 21:39:39 2012 +0200

    #530: target was not correctly converted to an absolute path in transform command

    The transform command was not refactored yet to deal with the phar packaging. This has
    been adapted and now it works like a charm.
    Also the file selection process has changed and only 50% of the previously selected files
    are now included and the size has dropped to 7mb instead of 15