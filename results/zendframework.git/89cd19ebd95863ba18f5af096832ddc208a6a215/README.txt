commit 89cd19ebd95863ba18f5af096832ddc208a6a215
Author: Evan Coury <me@evancoury.com>
Date:   Sat Dec 31 11:03:42 2011 -0700

    [Zend\Loader] Simplified StandardAutoloader::transformClassToFileName()

    Now using a simple preg_match with two str_replace() calls. This eliminates the
    calls to substr() and strpos() and seems to shave about 1% off the total
    execution time of the skeleton app. Regardless of the tiny performance gain,
    this change is worth while simply due to the simplification and increased
    readability of the code.

    StandardAutoloader tests still pass with this change. It should be functionally
    identical to what we had before.