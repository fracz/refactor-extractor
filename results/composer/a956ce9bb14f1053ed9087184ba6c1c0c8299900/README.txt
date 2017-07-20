commit a956ce9bb14f1053ed9087184ba6c1c0c8299900
Author: user <user@machina>
Date:   Sun Nov 24 10:55:25 2013 +0100

    Use 'cache-files-ttl' for cache gc, fixes #2441

    The configuration option 'cache-ttl' was used instead of 'cache-files-ttl' to determine
    whether or not a cache gc should be performed.

    * changed 'cache-ttl' to 'cache-files-ttl' to determine if a gc should be performed
    * refactored FileDownloader to allow for easier testing
    * added test to ensure that the gc is called with the proper config option