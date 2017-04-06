commit 3eec8c1a517f8b93a5afd15b7f83b33c5df7e54b
Author: Igor Minar <iiminar@gmail.com>
Date:   Sat Sep 25 15:29:48 2010 -0700

    Properly initialize cookie service in order to preserve existing cookies

    - previously the poller initialized the cookie cache too late which
      was causing previously existing cookies to be deleted by cookie service
    - refactored the poller api so that the addPollFn returns the added fn
    - fixed older cookie service tests
    - removed "this.$onEval(PRIORITY_LAST, update);" because it is not needed