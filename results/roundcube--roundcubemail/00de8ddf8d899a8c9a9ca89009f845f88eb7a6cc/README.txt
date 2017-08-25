commit 00de8ddf8d899a8c9a9ca89009f845f88eb7a6cc
Author: Aleksander Machniak <alec@alec.pl>
Date:   Wed Nov 6 13:11:31 2013 +0100

    Small performance improvements, use str_replace() instead of strtr(),
    do not parse query if there are no params to replace,
    keep one instance of (potentially long) query less in memory