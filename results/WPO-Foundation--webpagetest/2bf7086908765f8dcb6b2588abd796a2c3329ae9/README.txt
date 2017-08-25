commit 2bf7086908765f8dcb6b2588abd796a2c3329ae9
Author: Stefan Burnicki <stefan.burnicki@iteratec.de>
Date:   Wed Jun 22 09:45:28 2016 +0200

    refactor: Splitted loadPageData and getCsiInfo to prepare for multistep

    The new versions are more flexible when it comes to actual data access.
    Because of the caching-structure for CSI information, we're still going
    to need to use the run/cached/step info for multistep and not only
    depend on the TestPaths object