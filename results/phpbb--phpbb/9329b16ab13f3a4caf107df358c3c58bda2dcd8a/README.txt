commit 9329b16ab13f3a4caf107df358c3c58bda2dcd8a
Author: Igor Wiedler <igor@wiedler.ch>
Date:   Wed Nov 3 18:35:31 2010 +0100

    [task/acm-refactor] Refactor the ACM classes to have a common interface.

    They are now refered to as cache drivers rather than ACM classes. The
    additional utility functions from the original cache class have been
    moved to the cache_service. The class loader is now instantiated without
    a cache instance and passed one as soon as it is constructed to allow
    autoloading the cache classes.

    PHPBB3-9983